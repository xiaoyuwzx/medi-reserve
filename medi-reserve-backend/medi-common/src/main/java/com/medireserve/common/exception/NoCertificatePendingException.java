package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 无待审核证件异常
 */
public class NoCertificatePendingException extends BusinessException {
    public NoCertificatePendingException() {
        super(StatusCodeConstant.ERROR, MessageConstant.NO_CERTIFICATE_PENDING);
    }

    public NoCertificatePendingException(String message) {
        super(StatusCodeConstant.ERROR, message);
    }
}