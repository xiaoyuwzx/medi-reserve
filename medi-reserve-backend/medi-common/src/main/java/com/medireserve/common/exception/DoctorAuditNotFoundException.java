package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 医生审核数据不存在异常
 * 当医生已注册但没有对应的审核资料记录时抛出
 */
public class DoctorAuditNotFoundException extends BusinessException {

    public DoctorAuditNotFoundException() {
        super(StatusCodeConstant.DOCTOR_AUDIT_NOT_FOUND, MessageConstant.DOCTOR_AUDIT_NOT_FOUND);
    }

    public DoctorAuditNotFoundException(String message) {
        super(StatusCodeConstant.DOCTOR_AUDIT_NOT_FOUND, message);
    }
}