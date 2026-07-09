package com.medireserve.patient.service;

import com.medireserve.common.dto.ScheduleDetailVO;

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

}
