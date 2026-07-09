package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 预约记录不存在异常
 */
public class AppointmentNotFoundException extends BusinessException {

    public AppointmentNotFoundException() {
        super(StatusCodeConstant.APPOINTMENT_NOT_FOUND, MessageConstant.APPOINTMENT_NOT_FOUND);
    }

    public AppointmentNotFoundException(String message) {
        super(StatusCodeConstant.APPOINTMENT_NOT_FOUND, message);
    }
}