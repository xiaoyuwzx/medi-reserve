package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 排班下存在预约记录，无法删除异常
 */
public class ScheduleHasAppointmentsException extends BusinessException {

    public ScheduleHasAppointmentsException() {
        super(StatusCodeConstant.SCHEDULE_HAS_APPOINTMENTS, MessageConstant.SCHEDULE_HAS_APPOINTMENTS);
    }

    public ScheduleHasAppointmentsException(String message) {
        super(StatusCodeConstant.SCHEDULE_HAS_APPOINTMENTS, message);
    }
}