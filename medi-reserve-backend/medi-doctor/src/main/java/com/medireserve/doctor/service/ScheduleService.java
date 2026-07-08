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
     * @param doctorId
     * @param scheduleCreateDTO
     * @return
     */
    Schedule createSchedule(Long doctorId, ScheduleCreateDTO scheduleCreateDTO);

    /**
     * 查询医生排班列表
     * @param doctorId
     * @param scheduleQueryDTO
     * @return
     */
    List<Schedule> listSchedule(Long doctorId, @Valid ScheduleQueryDTO scheduleQueryDTO);

    /**
     * 根据排班ID查询排班数据
     * @param scheduleId
     * @return
     */
    Schedule getScheduleById(Long scheduleId);

    /**
     * 修改排班状态：停诊/恢复
     * @param scheduleId
     * @param targetStatus
     * @param currentDoctorId
     */
    void updateScheduleStatus(Long scheduleId, int targetStatus, Long currentDoctorId);

    /**
     * 删除排班
     * @param scheduleId
     * @param currentDoctorId
     */
    void deleteSchedule(Long scheduleId, Long currentDoctorId);

}
