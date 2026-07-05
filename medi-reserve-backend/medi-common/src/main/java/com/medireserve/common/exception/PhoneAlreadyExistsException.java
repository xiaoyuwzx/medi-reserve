package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 手机号已存在异常
 */
public class PhoneAlreadyExistsException extends BusinessException {

    public PhoneAlreadyExistsException() {
        super(StatusCodeConstant.PHONE_EXISTS, MessageConstant.PHONE_EXISTS);
    }

    public PhoneAlreadyExistsException(String message) {
        super(StatusCodeConstant.PHONE_EXISTS, message);
    }
}