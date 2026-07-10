package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 排班号源已满异常（挂号场景）
 * 当患者尝试预约号源已满的排班时抛出
 * 注意：区别于排班管理中的 ScheduleFullException（停诊场景）
 */
public class ScheduleAlreadyFullException extends BusinessException {

    public ScheduleAlreadyFullException() {
        super(StatusCodeConstant.SCHEDULE_ALREADY_FULL, MessageConstant.SCHEDULE_ALREADY_FULL);
    }

    public ScheduleAlreadyFullException(String message) {
        super(StatusCodeConstant.SCHEDULE_ALREADY_FULL, message);
    }
}