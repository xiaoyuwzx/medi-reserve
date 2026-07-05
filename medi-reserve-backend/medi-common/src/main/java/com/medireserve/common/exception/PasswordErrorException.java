package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 密码错误异常
 */
public class PasswordErrorException extends BusinessException {

    public PasswordErrorException() {
        super(StatusCodeConstant.PASSWORD_ERROR, MessageConstant.PASSWORD_ERROR);
    }

    public PasswordErrorException(String message) {
        super(StatusCodeConstant.PASSWORD_ERROR, message);
    }
}