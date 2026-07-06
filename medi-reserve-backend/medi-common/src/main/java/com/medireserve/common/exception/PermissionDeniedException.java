package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 权限不足异常（无操作权限）
 */
public class PermissionDeniedException extends BusinessException {

    public PermissionDeniedException() {
        super(StatusCodeConstant.FORBIDDEN, MessageConstant.PERMISSION_DENIED);
    }

    public PermissionDeniedException(String message) {
        super(StatusCodeConstant.FORBIDDEN, message);
    }
}