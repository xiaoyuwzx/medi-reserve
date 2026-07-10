package com.medireserve.doctor.service.impl;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.ScheduleCreateDTO;
import com.medireserve.common.dto.ScheduleQueryDTO;
import com.medireserve.common.entity.Schedule;
import com.medireserve.common.exception.*;
import com.medireserve.doctor.mapper.ScheduleMapper;
import com.medireserve.doctor.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班管理业务接口
 */
@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 获取推荐号源数
     * @param doctorId
     * @param scheduleDate
     * @param userInputMax
     * @return
     */
    @Override
    public Integer getRecommendedMaxCount(Long doctorId, LocalDate scheduleDate, int userInputMax) {

        log.info("计算推荐号源数，医生ID：{}，日期：{}，用户输入基准值：{}", doctorId, scheduleDate, userInputMax);

        // 调用内部算法计算推荐值（与之前逻辑相同）
        Integer recommended = recommendMaxCount(doctorId, scheduleDate, userInputMax);
        log.info("推荐号源数：{}（基准值：{}）", recommended, userInputMax);

        return recommended;

    }

    /**
     * 新增排班
     * @param doctorId
     * @param scheduleCreateDTO
     * @return
     */
    @Override
    @Transactional  //事务管理
    public Schedule createSchedule(Long doctorId, ScheduleCreateDTO scheduleCreateDTO) {

        log.info("开始新增排班，医生ID：{}，日期：{}，时段：{}", doctorId, scheduleCreateDTO.getScheduleDate(), scheduleCreateDTO.getPeriod());

        //防重校验：检查同一时间段是否已有排班
        int count = scheduleMapper.countByDoctorDatePeriod(doctorId, scheduleCreateDTO.getScheduleDate(), scheduleCreateDTO.getPeriod());
        if(count > 0){
            log.warn("排班重复，医生ID：{}，日期：{}，时段：{}", doctorId, scheduleCreateDTO.getScheduleDate(), scheduleCreateDTO.getPeriod());
            throw new ScheduleDuplicateException();
        }

        //构建排班实体
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleCreateDTO, schedule);
        schedule.setDoctorId(doctorId);
        //schedule.setMaxCount(scheduleCreateDTO.getMaxCount());
        schedule.setRemainingCount(scheduleCreateDTO.getMaxCount());    //初始值 == 最大值
        schedule.setStatus(StatusConstant.SCHEDULE_NORMAL); //状态初始为正常

        //保存进数据库
        scheduleMapper.insert(schedule);

        log.info("排班创建成功，ID：{}，号源数：{}", schedule.getId(), schedule.getMaxCount());

        return schedule;

    }

    /**
     * 查询医生排班列表
     * @param doctorId
     * @param scheduleQueryDTO
     * @return
     */
    @Override
    public List<Schedule> listSchedule(Long doctorId, ScheduleQueryDTO scheduleQueryDTO) {

        LocalDate startDate = null;
        LocalDate endDate = null;
        Integer status = null;
        if (scheduleQueryDTO != null) {
            startDate = scheduleQueryDTO.getStartDate();
            endDate = scheduleQueryDTO.getEndDate();
            status = scheduleQueryDTO.getStatus();
        }

        log.info("查询医生排班，医生ID：{}，日期范围：{} ~ {}，状态：{}", doctorId, startDate, endDate, status);

        return scheduleMapper.findByDoctorIdAndDateRange(doctorId, startDate, endDate, status);

    }

    /**
     * 根据排班ID查询排班数据
     * @param scheduleId
     * @return
     */
    @Override
    public Schedule getScheduleById(Long scheduleId) {

        log.info("根据ID查询排班数据中... , 排班ID：{}", scheduleId);

        Schedule schedule = scheduleMapper.findById(scheduleId);

        if (schedule == null) {
            log.warn("排班记录不存在，排班ID：{}", scheduleId);
            throw new ScheduleNotFoundException();
        }

        return schedule;

    }

    /**
     * 修改排班状态：停诊/恢复
     * @param scheduleId
     * @param targetStatus
     * @param currentDoctorId
     */
    @Override
    @Transactional  //事务管理
    public void updateScheduleStatus(Long scheduleId, int targetStatus, Long currentDoctorId) {

        log.info("更新排班状态，排班ID：{}，目标状态：{}", scheduleId, targetStatus);

        //查询排班是否存在
        Schedule schedule = scheduleMapper.findById(scheduleId);
        if(schedule == null){
            log.warn("排班不存在，排班ID：{}", scheduleId);
            throw new ScheduleNotFoundException();
        }

        // 校验权限
        if (!schedule.getDoctorId().equals(currentDoctorId)) {
            log.warn("医生ID不匹配，无权操作此排班，医生ID：{}，排班ID：{}", currentDoctorId, scheduleId);
            throw new PermissionDeniedException("您无权操作此排班");
        }

        //检验目标状态是否合法（只能为 1 或 2 ）
        if(!StatusConstant.SCHEDULE_NORMAL.equals(targetStatus) && !StatusConstant.SCHEDULE_STOPPED.equals(targetStatus)){
            log.warn("更新失败，目标状态不合法，排班ID：{}，目标状态：{}", scheduleId, targetStatus);
            throw new ScheduleStatusInvalidException();
        }


        //若排班状态以满，不允许手动停诊
        if(StatusConstant.SCHEDULE_FULL.equals(schedule.getStatus())){
            log.warn("当前排班号源已满，无法停诊，请先处理已挂号患者，排班ID：{}", scheduleId);
            throw new ScheduleFullException();
        }

        //更新数据库
        scheduleMapper.updateStatus(scheduleId, targetStatus);

        String actionMsg = StatusConstant.SCHEDULE_STOPPED.equals(targetStatus)
                ? MessageConstant.SCHEDULE_STOP_SUCCESS
                : MessageConstant.SCHEDULE_RESUME_SUCCESS;

        log.info("排班状态更新成功，ID：{}，{}", scheduleId, actionMsg);

    }

    /**
     * 删除排班
     * @param scheduleId
     * @param currentDoctorId
     */
    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId, Long currentDoctorId) {

        log.info("删除排班，排班ID：{}", scheduleId);

        //校验排班是否存在
        Schedule schedule = scheduleMapper.findById(scheduleId);
        if(schedule == null){
            log.warn("删除排班失败，排班记录不存在，排班ID：{}", scheduleId);
            throw new ScheduleNotFoundException();
        }

        // 校验权限
        if (!schedule.getDoctorId().equals(currentDoctorId)) {
            log.warn("医生ID不匹配，无权操作此排班，医生ID：{}，排班ID：{}", currentDoctorId, scheduleId);
            throw new PermissionDeniedException("您无权操作此排班");
        }

        //校验排班下是否有预约记录
        int appointmentCount = scheduleMapper.countAppointmentsByScheduleId(scheduleId);
        if(appointmentCount > 0){
            log.warn("排班下存在预约记录，禁止删除，排班ID：{}，预约数：{}", scheduleId, appointmentCount);
            throw new ScheduleHasAppointmentsException();
        }

        //删除排班
        scheduleMapper.deleteById(scheduleId);

        log.info("排班删除成功，排班ID：{}", scheduleId);

    }



    /*===================================================================  特有方法  ================================================================*/

    /**
     * 智能推荐初始号源数算法（基于历史就诊热度）
     * @param doctorId
     * @param scheduleDate
     * @param userRequestedMax
     * @return
     */
    private int recommendMaxCount(Long doctorId, LocalDate scheduleDate, Integer userRequestedMax) {
        /*
         *
         * 查询该医生过去四周同一天的历史就诊数据，计算出平均就诊率 historicalOccupancyRate
         * 根据 平均就诊率 计算推荐值 recommended
         * historicalOccupancyRate <= 40%     减号20%
         * historicalOccupancyRate >= 85%     加好20%
         *     40%  < 正常范围 < 85%          保持不变
         *
         * */

        //获取当前时间是星期几
        int dayOfWeek = scheduleDate.getDayOfWeek().getValue();

        Double historicalOccupancyRate = scheduleMapper.getHistoricalOccupancyRate(doctorId, dayOfWeek ,scheduleDate);

        //如果没有历史数据(新医生或者首次排班), 返回用户输入值
        if(historicalOccupancyRate == null || historicalOccupancyRate == 0.0){
            log.info("该医生无历史就诊数据，保持用户输入值：{}", userRequestedMax);
            return userRequestedMax;
        }

        //根据就诊率动态调整推荐值
        int recommended;
        if (historicalOccupancyRate >= 0.85) {
            // 就诊率超过85%，建议加号（增加20%）
            recommended = (int) Math.ceil(userRequestedMax * 1.2);
            log.debug("历史就诊率高（{}%），建议加号至：{}", (int)(historicalOccupancyRate * 100), recommended);
        } else if (historicalOccupancyRate <= 0.40) {
            // 就诊率低于40%，建议减号（减少20%）
            recommended = (int) Math.ceil(userRequestedMax * 0.8);
            log.debug("历史就诊率低（{}%），建议减号至：{}", (int)(historicalOccupancyRate * 100), recommended);
        } else {
            // 正常范围，保持用户请求的值
            recommended = userRequestedMax;
            log.debug("历史就诊率正常（{}%），保持原值：{}", (int)(historicalOccupancyRate * 100), recommended);
        }

        // 边界保护：号源数至少为1，最多100
        return Math.max(1, Math.min(100, recommended));

    }


    /**
     * 模拟历史就诊率（供演示）
     * 替换为真实数据库查询
     */
    /*private double simulateHistoricalOccupancyRate(Long doctorId, int dayOfWeek) {
        // 模拟不同医生的数据，让推荐效果更明显
        if (doctorId == 1 && dayOfWeek == 3) { // 医生1周三就诊率高
            return 0.92;
        } else if (doctorId == 1 && dayOfWeek == 6) { // 医生1周六就诊率低
            return 0.30;
        } else {
            return 0.65; // 默认中等
        }
    }*/

}
