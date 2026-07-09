package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 重复预约异常
 * 当患者对同一排班创建了待支付或已支付的预约时抛出
 */
public class AppointmentDuplicateException extends BusinessException {

    public AppointmentDuplicateException() {
        super(StatusCodeConstant.APPOINTMENT_DUPLICATE, MessageConstant.APPOINTMENT_DUPLICATE);
    }

    public AppointmentDuplicateException(String message) {
        super(StatusCodeConstant.APPOINTMENT_DUPLICATE, message);
    }
}