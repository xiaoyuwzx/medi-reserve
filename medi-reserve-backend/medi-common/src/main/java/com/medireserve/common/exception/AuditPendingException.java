package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 审核中异常（账号待审核，暂不可登录）
 */
public class AuditPendingException extends BusinessException {

    public AuditPendingException() {
        super(StatusCodeConstant.AUDIT_PENDING, MessageConstant.AUDIT_PENDING_MSG);
    }

    public AuditPendingException(String message) {
        super(StatusCodeConstant.AUDIT_PENDING, message);
    }
}