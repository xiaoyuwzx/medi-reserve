package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 支付失败异常
 * 当更新支付状态时数据库无影响时抛出
 */
public class PaymentFailedException extends BusinessException {

    public PaymentFailedException() {
        super(StatusCodeConstant.APPOINTMENT_PAY_FAILED, MessageConstant.APPOINTMENT_PAY_FAILED);
    }

    public PaymentFailedException(String message) {
        super(StatusCodeConstant.APPOINTMENT_PAY_FAILED, message);
    }
}