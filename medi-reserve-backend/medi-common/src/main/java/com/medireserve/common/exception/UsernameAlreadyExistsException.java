package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 用户名已存在异常
 */
public class UsernameAlreadyExistsException extends BusinessException {

    public UsernameAlreadyExistsException() {
        super(StatusCodeConstant.USERNAME_EXISTS, MessageConstant.USERNAME_EXISTS);
    }

    public UsernameAlreadyExistsException(String message) {
        super(StatusCodeConstant.USERNAME_EXISTS, message);
    }
}