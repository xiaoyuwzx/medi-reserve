package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 审核结果参数无效异常
 */
public class InvalidAuditResultException extends BusinessException {
    public InvalidAuditResultException() {
        super(StatusCodeConstant.PARAM_ERROR, MessageConstant.INVALID_AUDIT_RESULT);
    }

    public InvalidAuditResultException(String message) {
        super(StatusCodeConstant.PARAM_ERROR, message);
    }
}