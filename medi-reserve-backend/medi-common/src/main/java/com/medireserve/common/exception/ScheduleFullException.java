package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 排班号源已满，无法停诊异常
 */
public class ScheduleFullException extends BusinessException {

    public ScheduleFullException() {
        super(StatusCodeConstant.SCHEDULE_FULL, MessageConstant.SCHEDULE_FULL_STOP_ERROR);
    }

    public ScheduleFullException(String message) {
        super(StatusCodeConstant.SCHEDULE_FULL, message);
    }
}