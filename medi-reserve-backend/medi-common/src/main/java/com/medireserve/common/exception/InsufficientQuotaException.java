package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 号源不足异常
 * 当扣减号源时 remaining_count <= 0 时抛出
 */
public class InsufficientQuotaException extends BusinessException {

    public InsufficientQuotaException() {
        super(StatusCodeConstant.INSUFFICIENT_QUOTA, MessageConstant.INSUFFICIENT_QUOTA);
    }

    public InsufficientQuotaException(String message) {
        super(StatusCodeConstant.INSUFFICIENT_QUOTA, message);
    }
}