package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 预约已支付异常
 * 当重复支付一个已支付的预约时抛出（幂等场景）
 */
public class AppointmentAlreadyPaidException extends BusinessException {

    public AppointmentAlreadyPaidException() {
        super(StatusCodeConstant.APPOINTMENT_ALREADY_PAID, MessageConstant.APPOINTMENT_ALREADY_PAID);
    }

    public AppointmentAlreadyPaidException(String message) {
        super(StatusCodeConstant.APPOINTMENT_ALREADY_PAID, message);
    }
}