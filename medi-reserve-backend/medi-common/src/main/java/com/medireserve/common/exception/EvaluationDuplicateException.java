package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 重复评价异常
 * 当一个预约已被评价过，再次提交时抛出
 */
public class EvaluationDuplicateException extends BusinessException {

    public EvaluationDuplicateException() {
        super(StatusCodeConstant.EVALUATION_DUPLICATE, MessageConstant.EVALUATION_DUPLICATE);
    }

    public EvaluationDuplicateException(String message) {
        super(StatusCodeConstant.EVALUATION_DUPLICATE, message);
    }
}