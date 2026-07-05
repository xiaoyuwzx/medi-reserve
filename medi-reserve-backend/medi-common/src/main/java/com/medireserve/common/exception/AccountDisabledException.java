package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 账号已禁用异常
 */
public class AccountDisabledException extends BusinessException {

    public AccountDisabledException() {
        super(StatusCodeConstant.ACCOUNT_DISABLED, MessageConstant.ACCOUNT_DISABLED);
    }

    public AccountDisabledException(String message) {
        super(StatusCodeConstant.ACCOUNT_DISABLED, message);
    }
}