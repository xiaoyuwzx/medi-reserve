package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 排班不存在异常
 */
public class ScheduleNotFoundException extends BusinessException {

    public ScheduleNotFoundException() {
        super(StatusCodeConstant.SCHEDULE_NOT_FOUND, MessageConstant.SCHEDULE_NOT_FOUND);
    }

    public ScheduleNotFoundException(String message) {
        super(StatusCodeConstant.SCHEDULE_NOT_FOUND, message);
    }
}