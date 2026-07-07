package com.medireserve.doctor.service;

import com.medireserve.common.dto.ScheduleCreateDTO;
import com.medireserve.common.dto.ScheduleQueryDTO;
import com.medireserve.common.entity.Schedule;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班管理业务接口
 */
public interface  ScheduleService {

    /**
     * 获取推荐号源数
     * @param doctorId
     * @param scheduleDate
     * @param userInputMax
     * @return
     */
    Integer getRecommendedMaxCount(Long doctorId, LocalDate scheduleDate, int userInputMax);

    /**
     * 新增排班
     * @param scheduleCreateDTO
     * @return
     */
    Schedule createSchedule(ScheduleCreateDTO scheduleCreateDTO);

    /**
     * 查询医生排班列表
     * @param doctorId
     * @param scheduleQueryDTO
     * @return
     */
    List<Schedule> listSchedule(Long doctorId, @Valid ScheduleQueryDTO scheduleQueryDTO);

}
