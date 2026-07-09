package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 预约状态不是待支付异常
 * 当支付时预约状态不是 0（待支付）时抛出
 */
public class AppointmentNotPendingException extends BusinessException {

    public AppointmentNotPendingException() {
        super(StatusCodeConstant.APPOINTMENT_NOT_PENDING, MessageConstant.APPOINTMENT_NOT_PENDING);
    }

    public AppointmentNotPendingException(String message) {
        super(StatusCodeConstant.APPOINTMENT_NOT_PENDING, message);
    }
}