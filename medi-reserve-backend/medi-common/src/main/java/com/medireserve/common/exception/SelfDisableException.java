package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 尝试禁用自己异常
 */
public class SelfDisableException extends BusinessException {
    public SelfDisableException() {
        super(StatusCodeConstant.FORBIDDEN, MessageConstant.SELF_DISABLE_ERROR);
    }

    public SelfDisableException(String message) {
        super(StatusCodeConstant.FORBIDDEN, message);
    }
}