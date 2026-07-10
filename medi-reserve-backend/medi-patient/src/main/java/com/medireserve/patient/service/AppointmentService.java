package com.medireserve.patient.service;

import com.medireserve.common.dto.AppointmentCreateDTO;
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
}
