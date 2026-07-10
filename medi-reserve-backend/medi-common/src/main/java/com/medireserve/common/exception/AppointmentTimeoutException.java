package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 预约已超时异常
 * 当支付时发现预约已超过30分钟支付时限时抛出
 */
public class AppointmentTimeoutException extends BusinessException {

    public AppointmentTimeoutException() {
        super(StatusCodeConstant.APPOINTMENT_TIMEOUT, MessageConstant.APPOINTMENT_TIMEOUT);
    }

    public AppointmentTimeoutException(String message) {
        super(StatusCodeConstant.APPOINTMENT_TIMEOUT, message);
    }
}