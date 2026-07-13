package com.medireserve.common.exception;

import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 账号被锁定异常（登录失败次数过多）
 */
public class AccountLockedException extends BusinessException {

    public AccountLockedException(String message) {
        super(StatusCodeConstant.ACCOUNT_LOCKED, message);
    }
}