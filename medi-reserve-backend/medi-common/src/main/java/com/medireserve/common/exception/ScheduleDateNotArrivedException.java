package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 就诊日期尚未到来异常
 * 当患者尝试评价未来日期的就诊时抛出
 */
public class ScheduleDateNotArrivedException extends BusinessException {

    public ScheduleDateNotArrivedException() {
        super(StatusCodeConstant.SCHEDULE_DATE_NOT_ARRIVED, MessageConstant.SCHEDULE_DATE_NOT_ARRIVED);
    }

    public ScheduleDateNotArrivedException(String message) {
        super(StatusCodeConstant.SCHEDULE_DATE_NOT_ARRIVED, message);
    }
}