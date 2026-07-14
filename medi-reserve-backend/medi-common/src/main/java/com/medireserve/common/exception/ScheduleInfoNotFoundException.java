package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 排班信息不存在异常
 * 当评价时关联的排班记录找不到时抛出
 */
public class ScheduleInfoNotFoundException extends BusinessException {

    public ScheduleInfoNotFoundException() {
        super(StatusCodeConstant.SCHEDULE_INFO_NOT_FOUND, MessageConstant.SCHEDULE_INFO_NOT_FOUND);
    }

    public ScheduleInfoNotFoundException(String message) {
        super(StatusCodeConstant.SCHEDULE_INFO_NOT_FOUND, message);
    }
}