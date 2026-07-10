package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 排班已停诊异常
 * 当患者尝试预约已停诊的排班时抛出
 */
public class ScheduleStoppedException extends BusinessException {

    public ScheduleStoppedException() {
        super(StatusCodeConstant.SCHEDULE_STOPPED, MessageConstant.SCHEDULE_ALREADY_STOPPED);
    }

    public ScheduleStoppedException(String message) {
        super(StatusCodeConstant.SCHEDULE_STOPPED, message);
    }
}