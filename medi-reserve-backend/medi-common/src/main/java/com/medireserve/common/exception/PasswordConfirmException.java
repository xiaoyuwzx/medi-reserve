package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 两次密码输入不一致异常
 */
public class PasswordConfirmException extends BusinessException {
    public PasswordConfirmException() {
        super(StatusCodeConstant.PARAM_ERROR, MessageConstant.PASSWORD_CONFIRM_ERROR);
    }

    public PasswordConfirmException(String message) {
        super(StatusCodeConstant.PARAM_ERROR, message);
    }
}