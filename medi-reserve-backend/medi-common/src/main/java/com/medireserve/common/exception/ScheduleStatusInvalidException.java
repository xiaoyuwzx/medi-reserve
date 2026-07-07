package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 排班目标状态不合法异常
 */
public class ScheduleStatusInvalidException extends BusinessException {

    public ScheduleStatusInvalidException() {
        super(StatusCodeConstant.SCHEDULE_STATUS_INVALID, MessageConstant.SCHEDULE_STATUS_INVALID);
    }

    public ScheduleStatusInvalidException(String message) {
        super(StatusCodeConstant.SCHEDULE_STATUS_INVALID, message);
    }
}