package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 审核操作失败异常
 * 当数据库更新影响行数为0时抛出（表示数据未被修改）
 */
public class AuditOperationFailedException extends BusinessException {

    public AuditOperationFailedException() {
        super(StatusCodeConstant.AUDIT_OPERATION_FAILED, MessageConstant.AUDIT_OPERATION_FAILED);
    }

    public AuditOperationFailedException(String message) {
        super(StatusCodeConstant.AUDIT_OPERATION_FAILED, message);
    }
}