package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 账号不存在异常
 */
public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException() {
        super(StatusCodeConstant.ACCOUNT_NOT_EXIST, MessageConstant.ACCOUNT_NOT_FOUND);
    }

    public AccountNotFoundException(String message) {
        super(StatusCodeConstant.ACCOUNT_NOT_EXIST, message);
    }
}