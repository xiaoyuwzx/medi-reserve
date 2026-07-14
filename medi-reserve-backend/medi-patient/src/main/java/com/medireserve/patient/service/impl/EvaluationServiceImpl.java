package com.medireserve.patient.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.DoctorHotVO;
import com.medireserve.common.dto.EvaluationCreateDTO;
import com.medireserve.common.dto.MyEvaluationVO;
import com.medireserve.common.entity.Appointment;
import com.medireserve.common.entity.Evaluation;
import com.medireserve.common.entity.Schedule;
import com.medireserve.common.exception.BusinessException;
import com.medireserve.common.exception.PermissionDeniedException;
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
            // TODO : 新建预约不存在异常类
            throw new BusinessException("预约不存在");
        }

        //校验预约归属
        if(!appointment.getPatientId().equals(patientId)){
            log.warn("预约不属于当前用户，预约ID：{}，患者ID：{}", appointmentId, patientId);
            throw new PermissionDeniedException("该预约不属于您");
        }

        //校验预约状态(必须是已支付1或者已完成2)
        if(StatusConstant.APPOINTMENT_PENDING.equals(appointment.getStatus())){
            log.warn("预约状态不符合评价条件，预约ID：{}，当前状态：{}", appointmentId, appointment.getStatus());
            // TODO : 新建预约尚未就诊不能评价异常类
            throw new BusinessException("该预约尚未就诊，暂不能评价");
        }

        //校验排班日期(必须已过就诊日)
        Schedule schedule = appointmentMapper.findByScheduleId(appointment.getScheduleId());
        if(schedule == null){
            log.warn("排班不存在，排班ID：{}", appointment.getScheduleId());
            // TODO : 新建排班信息不存在异常类
            throw new BusinessException("排班信息不存在");
        }
        if(schedule.getScheduleDate().isAfter(LocalDate.now())){
            log.warn("就诊日期尚未到来，排班ID：{}，排班日期：{}", schedule.getId(), schedule.getScheduleDate());
            // TODO : 新建就诊日期尚未到来异常类
            throw new BusinessException("就诊日期尚未到来，请就诊后再评价");
        }

        //校验是否已评价
        int existCount = evaluationMapper.countByAppointmentId(appointmentId);
        if(existCount > 0){
            log.warn("重复评价，预约ID：{}", appointmentId);
            // TODO : 新建已评价过该预约异常类
            throw new BusinessException("您已评价过该预约");
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
            throw new BusinessException("评价提交失败，请稍后重试");
        }

        log.info("评价创建成功，评价ID：{}, 预约ID：{}", evaluation.getId(), appointmentId);

        //刷新热门医生缓存
        //异步或同步刷新排行榜
        try {
            refreshHotDoctorCache();
            log.info("热门医生缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新热门医生缓存失败，评价ID：{}", evaluation.getId());
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
            throw new BusinessException("缓存刷新失败");
        }
    }


}
