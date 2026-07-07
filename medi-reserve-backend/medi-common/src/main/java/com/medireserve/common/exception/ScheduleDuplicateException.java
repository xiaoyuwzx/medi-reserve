package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 排班重复异常（同一医生同一天同一时段已有排班）
 */
public class ScheduleDuplicateException extends BusinessException {

    public ScheduleDuplicateException() {
        super(StatusCodeConstant.SCHEDULE_DUPLICATE, MessageConstant.SCHEDULE_DUPLICATE);
    }

    public ScheduleDuplicateException(String message) {
        super(StatusCodeConstant.SCHEDULE_DUPLICATE, message);
    }
}