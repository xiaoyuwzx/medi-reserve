package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 系统繁忙异常
 * 当获取分布式锁超时时抛出
 */
public class SystemBusyException extends BusinessException {

    public SystemBusyException() {
        super(StatusCodeConstant.SYSTEM_BUSY, MessageConstant.SYSTEM_BUSY);
    }

    public SystemBusyException(String message) {
        super(StatusCodeConstant.SYSTEM_BUSY, message);
    }
}