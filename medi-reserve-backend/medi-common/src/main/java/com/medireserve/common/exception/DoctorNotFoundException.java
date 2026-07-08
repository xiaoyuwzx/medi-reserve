package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 医生不存在异常
 */
public class DoctorNotFoundException extends BusinessException {

    public DoctorNotFoundException() {
        super(StatusCodeConstant.DOCTOR_NOT_FOUND, MessageConstant.DOCTOR_NOT_FOUND);
    }

    public DoctorNotFoundException(String message) {
        super(StatusCodeConstant.DOCTOR_NOT_FOUND, message);
    }
}