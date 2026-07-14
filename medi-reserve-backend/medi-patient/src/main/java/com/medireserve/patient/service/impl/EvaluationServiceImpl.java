package com.medireserve.patient.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.common.constant.EvaluationStatusConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.DoctorHotVO;
import com.medireserve.common.dto.EvaluationCreateDTO;
import com.medireserve.common.dto.EvaluationListVO;
import com.medireserve.common.dto.MyEvaluationVO;
import com.medireserve.common.entity.Appointment;
import com.medireserve.common.entity.Evaluation;
import com.medireserve.common.entity.Schedule;
import com.medireserve.common.exception.*;
import com.medireserve.patient.mapper.AppointmentMapper;
import com.medireserve.patient.mapper.EvaluationMapper;
import com.medireserve.patient.service.EvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 评价服务实现类
 * 包含所有评价相关的业务逻辑和校验
 */
@Slf4j
@Service
public class EvaluationServiceImpl implements EvaluationService {

    /**
     * Redis 热门医生排行榜 Key
     */
    private static final String REDIS_HOT_DOCTORS_KEY = "hot:doctors";

    /**
     * 排行榜缓存有效期（30分钟）
     * 即使定时任务未触发，缓存过期后会从数据库回源
     */
    private static final long CACHE_TIMEOUT_MINUTES = 30;

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 创建评价
     * @param patientId 当前登录患者ID
     * @param createDTO 评价创建参数
     * @return
     */
    @Override
    @Transactional
    public Evaluation createEvaluation(Long patientId, EvaluationCreateDTO createDTO) {

        Long appointmentId = createDTO.getAppointmentId();

        log.info("开始创建评价，患者ID：{}，预约ID：{}", patientId, appointmentId);

        //校验预约信息
        Appointment appointment = appointmentMapper.findById(appointmentId);
        if(appointment == null){
            log.warn("预约不存在，预约ID：{}", appointmentId);
            throw new AppointmentNotFoundException();
        }

        //校验预约归属
        if(!appointment.getPatientId().equals(patientId)){
            log.warn("预约不属于当前用户，预约ID：{}，患者ID：{}", appointmentId, patientId);
            throw new PermissionDeniedException("该预约不属于您");
        }

        //校验预约状态(必须是已支付1或者已完成2)
        if(!StatusConstant.APPOINTMENT_PAID.equals(appointment.getStatus())
                && !StatusConstant.APPOINTMENT_COMPLETED.equals(appointment.getStatus())){
            log.warn("预约状态不符合评价条件，预约ID：{}，当前状态：{}", appointmentId, appointment.getStatus());
            throw new AppointmentNotEvaluableException();
        }

        //校验排班日期(必须已过就诊日)
        Schedule schedule = appointmentMapper.findByScheduleId(appointment.getScheduleId());
        if(schedule == null){
            log.warn("排班不存在，排班ID：{}", appointment.getScheduleId());
            throw new ScheduleInfoNotFoundException();
        }
        if(schedule.getScheduleDate().isAfter(LocalDate.now())){
            log.warn("就诊日期尚未到来，排班ID：{}，排班日期：{}", schedule.getId(), schedule.getScheduleDate());
            throw new ScheduleDateNotArrivedException();
        }

        //校验是否已评价
        int existCount = evaluationMapper.countByAppointmentId(appointmentId);
        if(existCount > 0){
            log.warn("重复评价，预约ID：{}", appointmentId);
            throw new EvaluationDuplicateException();
        }

        //构建评价实体
        Evaluation evaluation = new Evaluation();
        evaluation.setAppointmentId(appointmentId);
        evaluation.setPatientId(patientId);
        evaluation.setDoctorId(appointment.getDoctorId());
        evaluation.setScheduleId(appointment.getScheduleId());
        evaluation.setScore(createDTO.getScore());
        evaluation.setContent(createDTO.getContent());
        evaluation.setIsAnonymous(createDTO.getIsAnonymous() ? 1 : 0);

        //保存进数据库
        int rows = evaluationMapper.insert(evaluation);
        if(rows == 0){
            log.warn("保存评价失败，预约ID：{}", appointmentId);
            throw new EvaluationCreateFailedException();
        }

        log.info("评价创建成功，评价ID：{}, 预约ID：{}", evaluation.getId(), appointmentId);

        //刷新热门医生缓存
        //异步或同步刷新排行榜
        try {
            refreshHotDoctorCache();
            log.info("热门医生缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新热门医生缓存失败，评价ID：{}", evaluation.getId(), e);
        }

        return evaluation;

    }

    /**
     * 查询我的评价列表(分页)
     * @param patientId 当前登录患者ID
     * @param page 页码
     * @param size 每页条数
     * @return
     */
    @Override
    public PageInfo<MyEvaluationVO> getMyEvaluations(Long patientId, int page, int size) {

        log.info("查询我的评价列表，患者ID：{}，页码：{}，每页：{}", patientId, page, size);

        PageHelper.startPage(page, size);

        List<MyEvaluationVO> list = evaluationMapper.findByPatientId(patientId);

        PageInfo<MyEvaluationVO> pageInfo = new PageInfo<>(list);

        log.info("查询完成，总记录数：{}", pageInfo.getTotal());

        return pageInfo;

    }

    /**
     * 查询医生的评价列表(公开访问，分页)
     * @param doctorId 医生ID
     * @param page 页码
     * @param size 每页条数
     * @return
     */
    @Override
    public PageInfo<EvaluationListVO> getDoctorEvaluations(Long doctorId, int page, int size) {

        log.info("查询医生评价列表，医生ID：{}，页码：{}，每页：{}", doctorId, page, size);

        PageHelper.startPage(page, size);

        List<EvaluationListVO> list = evaluationMapper.findByDoctorId(doctorId);

        PageInfo<EvaluationListVO> pageInfo = new PageInfo<>(list);

        log.info("查询完成，总记录数：{}", pageInfo.getTotal());

        return pageInfo;

    }

    /**
     * 删除评价(软删除)
     * @param evaluationId 评价ID
     * @param patientId 当前登录患者ID
     */
    @Override
    @Transactional
    public void deleteEvaluation(Long evaluationId, Long patientId) {

        log.info("删除评价，评价ID：{}，患者ID：{}", evaluationId, patientId);

        //查询评价
        Evaluation evaluation = evaluationMapper.findById(evaluationId);
        if(evaluation == null){
            log.warn("评价不存在，评价ID：{}", evaluationId);
            throw new EvaluationNotFoundException();
        }

        //校验归属
        if(!evaluation.getPatientId().equals(patientId)){
            log.warn("无权删除该评价，评价ID：{}，患者ID：{}", evaluationId, patientId);
            throw new PermissionDeniedException("您无权删除该评价");
        }

        //校验状态(不能重复删除)
        if(EvaluationStatusConstant.HIDDEN.equals(evaluation.getStatus())){
            log.warn("评价已删除，评价ID：{}", evaluationId);
            throw new EvaluationAlreadyDeletedException();
        }

        //执行软删除
        int rows = evaluationMapper.softDelete(evaluationId);
        if(rows == 0){
            log.error("删除评价失败，评价ID：{}", evaluationId);
            throw new EvaluationDeleteFailedException();
        }

        log.info("评价删除成功，评价ID：{}", evaluationId);

        //刷新热门医生缓存
        //异步或同步刷新排行榜
        try {
            refreshHotDoctorCache();
            log.info("热门医生缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新热门医生缓存失败，评价ID：{}", evaluationId, e);
        }

    }

    /**
     * 获取热门医生排行榜
     * 优先从Redis中读取，缓存为命中时查询数据库
     * @return
     */
    @Override
    public List<DoctorHotVO> getHotDoctors() {

        log.info("获取热门医生排行榜");

        //尝试从Redis中读取
        try {
            List<DoctorHotVO> cachedList = (List<DoctorHotVO>) redisTemplate.opsForValue().get(REDIS_HOT_DOCTORS_KEY);
            if(cachedList != null && !cachedList.isEmpty()){
                log.info("热门医生排行榜缓存命中，共 {} 条", cachedList.size());
                return cachedList;
            }
            log.info("热门医生排行榜缓存未命中，回源查询数据库");
        } catch (Exception e) {
            log.warn("Redis读取失败，降级到数据库查询", e);
        }

        //从数据库查询
        List<DoctorHotVO> hotDoctors = evaluationMapper.findHotDoctors(10);

        //写入Redis缓存
        try {
            if(hotDoctors != null && !hotDoctors.isEmpty()){
                redisTemplate.opsForValue().set(REDIS_HOT_DOCTORS_KEY, hotDoctors, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.info("热门医生排行榜缓存写入成功，共 {} 条", hotDoctors.size());
            }else {
                //空数据也缓存(防穿透), 有效期缩短为5分钟
                redisTemplate.opsForValue().set(REDIS_HOT_DOCTORS_KEY, hotDoctors, 5, TimeUnit.MINUTES);
                log.info("热门医生排行榜为空，缓存空值，有效期5分钟");
            }
        } catch (Exception e) {
            log.warn("Redis写入失败，不影响返回", e);
        }

        return hotDoctors;

    }

    /**
     * 刷新热门医生缓存
     * 重新从数据库计算排行榜并写入 Redis
     */
    @Override
    public void refreshHotDoctorCache() {

        log.info("开始刷新热门医生缓存");

        try {
            // 直接从数据库查询最新数据
            List<DoctorHotVO> hotDoctors = evaluationMapper.findHotDoctors(10);

            if (hotDoctors != null && !hotDoctors.isEmpty()) {
                redisTemplate.opsForValue().set(REDIS_HOT_DOCTORS_KEY, hotDoctors, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.info("热门医生缓存刷新成功，共 {} 条", hotDoctors.size());
            } else {
                // 无数据时缓存空对象，有效期5分钟
                redisTemplate.opsForValue().set(REDIS_HOT_DOCTORS_KEY, hotDoctors, 5, TimeUnit.MINUTES);
                log.info("热门医生数据为空，缓存空值");
            }
        } catch (Exception e) {
            log.error("刷新热门医生缓存失败", e);
            throw new CacheRefreshFailedException();
        }
    }


}
