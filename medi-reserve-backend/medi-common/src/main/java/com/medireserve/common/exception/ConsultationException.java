package com.medireserve.common.exception;

import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 问诊通用业务异常（带自定义状态码）
 */
public class ConsultationException extends BusinessException {

    public ConsultationException(String message) {
        super(message);
    }

    public ConsultationException(Integer code, String message) {
        super(code, message);
    }
}