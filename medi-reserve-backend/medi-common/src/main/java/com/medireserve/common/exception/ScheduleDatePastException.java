package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 排班日期已过异常
 * 当患者尝试挂已经过去的日期的号时抛出
 */
public class ScheduleDatePastException extends BusinessException {

    public ScheduleDatePastException() {
        super(StatusCodeConstant.SCHEDULE_DATE_PAST, MessageConstant.SCHEDULE_DATE_PAST);
    }

    public ScheduleDatePastException(String message) {
        super(StatusCodeConstant.SCHEDULE_DATE_PAST, message);
    }
}