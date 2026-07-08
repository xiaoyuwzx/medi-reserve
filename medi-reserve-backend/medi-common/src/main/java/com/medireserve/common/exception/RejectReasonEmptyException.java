package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 驳回原因为空异常
 * 审核驳回时必须填写驳回原因
 */
public class RejectReasonEmptyException extends BusinessException {

    public RejectReasonEmptyException() {
        super(StatusCodeConstant.DOCTOR_AUDIT_REJECT_REASON_EMPTY, MessageConstant.DOCTOR_AUDIT_REJECT_REASON_EMPTY);
    }

    public RejectReasonEmptyException(String message) {
        super(StatusCodeConstant.DOCTOR_AUDIT_REJECT_REASON_EMPTY, message);
    }
}