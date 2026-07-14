package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 预约尚未就诊，不能评价异常
 * 当患者尝试评价未就诊的预约时抛出
 */
public class AppointmentNotEvaluableException extends BusinessException {

    public AppointmentNotEvaluableException() {
        super(StatusCodeConstant.APPOINTMENT_NOT_EVALUABLE, MessageConstant.APPOINTMENT_NOT_EVALUABLE);
    }

    public AppointmentNotEvaluableException(String message) {
        super(StatusCodeConstant.APPOINTMENT_NOT_EVALUABLE, message);
    }
}