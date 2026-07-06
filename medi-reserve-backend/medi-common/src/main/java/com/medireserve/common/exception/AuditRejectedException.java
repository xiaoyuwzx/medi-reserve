package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 审核驳回异常（账号审核未通过）
 */
public class AuditRejectedException extends BusinessException {

    public AuditRejectedException() {
        super(StatusCodeConstant.AUDIT_REJECTED, MessageConstant.AUDIT_REJECTED_MSG);
    }

    public AuditRejectedException(String message) {
        super(StatusCodeConstant.AUDIT_REJECTED, message);
    }
}