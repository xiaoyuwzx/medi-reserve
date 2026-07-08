package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 医生已审核异常（重复审核操作）
 */
public class DoctorAlreadyAuditedException extends BusinessException {

    public DoctorAlreadyAuditedException() {
        super(StatusCodeConstant.DOCTOR_ALREADY_AUDITED, MessageConstant.DOCTOR_ALREADY_AUDITED);
    }

    public DoctorAlreadyAuditedException(String message) {
        super(StatusCodeConstant.DOCTOR_ALREADY_AUDITED, message);
    }
}