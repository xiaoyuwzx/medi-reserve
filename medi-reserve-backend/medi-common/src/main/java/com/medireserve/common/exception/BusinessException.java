package com.medireserve.common.exception;

import lombok.Getter;

/**
 * 业务异常基类
 * 所有业务异常都继承此类，便于全局统一处理
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;  // 自定义错误码

    public BusinessException(String message) {
        super(message);
        this.code = null;  // 使用默认错误码（由全局处理器决定）
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
    }
}