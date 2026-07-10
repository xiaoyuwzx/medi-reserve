package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 系统异常
 * 当发生不可预料的系统级错误时抛出（如分布式锁中断、数据库连接失败等）
 */
public class SystemException extends BusinessException {

    public SystemException() {
        super(StatusCodeConstant.SYSTEM_ERROR, MessageConstant.SYSTEM_ERROR);
    }

    public SystemException(String message) {
        super(StatusCodeConstant.SYSTEM_ERROR, message);
    }
}