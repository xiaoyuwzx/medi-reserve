package com.medireserve.patient.service;

import com.medireserve.common.dto.AppointmentCreateDTO;
import com.medireserve.common.dto.AppointmentListVO;
import com.medireserve.common.dto.ScheduleDetailVO;
import com.medireserve.common.entity.Appointment;
import jakarta.validation.Valid;

/**
 * 预约挂号：挂号下单、支付、查询排班等
 */
public interface AppointmentService {

    /**
     * 查询排班详细
     * @param scheduleId
     * @return
     */
    ScheduleDetailVO getScheduleDetail(Long scheduleId);

    /**
     * 创建预约(下单)
     * @param patientId
     * @param appointmentCreateDTO
     * @return
     */
    Appointment createAppointment(Long patientId, AppointmentCreateDTO appointmentCreateDTO);

    /**
     * 模拟支付
     * @param appointmentId
     * @param patientId
     */
    void payAppointment(Long appointmentId, Long patientId);

    /**
     * 查询我的预约列表（分页）
     * @param patientId
     * @param status 预约状态筛选（可选）
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    com.github.pagehelper.PageInfo<AppointmentListVO> getMyAppointments(Long patientId, Integer status, int page, int size);
}
