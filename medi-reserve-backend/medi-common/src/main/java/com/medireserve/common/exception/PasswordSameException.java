package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 新旧密码相同异常
 */
public class PasswordSameException extends BusinessException {
    public PasswordSameException() {
        super(StatusCodeConstant.PARAM_ERROR, MessageConstant.PASSWORD_SAME_ERROR);
    }

    public PasswordSameException(String message) {
        super(StatusCodeConstant.PARAM_ERROR, message);
    }
}