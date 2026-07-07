package com.medireserve.doctor.service.impl;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.ScheduleCreateDTO;
import com.medireserve.common.entity.Schedule;
import com.medireserve.common.exception.BusinessException;
import com.medireserve.doctor.mapper.ScheduleMapper;
import com.medireserve.doctor.service.ScheduleService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 排班管理业务接口
 */
@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    ScheduleMapper scheduleMapper;

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
     * @param scheduleCreateDTO
     * @return
     */
    @Override
    @Transactional  //事务管理
    public Schedule createSchedule(ScheduleCreateDTO scheduleCreateDTO) {

        log.info("开始新增排班，医生ID：{}，日期：{}，时段：{}", scheduleCreateDTO.getDoctorId(), scheduleCreateDTO.getScheduleDate(), scheduleCreateDTO.getPeriod());

        //防重校验：检查同一时间段是否已有排班
        int count = scheduleMapper.countByDoctorDatePeriod(scheduleCreateDTO.getDoctorId(), scheduleCreateDTO.getScheduleDate(), scheduleCreateDTO.getPeriod());
        if(count > 0){
            log.warn("排班重复，医生ID：{}，日期：{}，时段：{}", scheduleCreateDTO.getDoctorId(), scheduleCreateDTO.getScheduleDate(), scheduleCreateDTO.getPeriod());
            // TODO : 增加已有排班异常类
            throw new BusinessException(MessageConstant.SCHEDULE_DUPLICATE);
        }

        //构建排班实体
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleCreateDTO, schedule);
        schedule.setMaxCount(scheduleCreateDTO.getMaxCount());
        schedule.setRemainingCount(scheduleCreateDTO.getMaxCount());    //初始值 == 最大值
        schedule.setStatus(StatusConstant.SCHEDULE_NORMAL); //状态初始为正常

        //保存进数据库
        scheduleMapper.insert(schedule);

        log.info("排班创建成功，ID：{}，号源数：{}", schedule.getId(), schedule.getMaxCount());

        return schedule;

    }

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

        double historicalOccupancyRate = simulateHistoricalOccupancyRate(doctorId, dayOfWeek);

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
     * TODO: 替换为真实数据库查询
     */
    private double simulateHistoricalOccupancyRate(Long doctorId, int dayOfWeek) {
        // 模拟不同医生的数据，让推荐效果更明显
        if (doctorId == 1 && dayOfWeek == 3) { // 医生1周三就诊率高
            return 0.92;
        } else if (doctorId == 1 && dayOfWeek == 6) { // 医生1周六就诊率低
            return 0.30;
        } else {
            return 0.65; // 默认中等
        }
    }

}
