package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 评价已删除异常
 * 当尝试删除已被删除的评价时抛出
 */
public class EvaluationAlreadyDeletedException extends BusinessException {

    public EvaluationAlreadyDeletedException() {
        super(StatusCodeConstant.EVALUATION_ALREADY_DELETED, MessageConstant.EVALUATION_ALREADY_DELETED);
    }

    public EvaluationAlreadyDeletedException(String message) {
        super(StatusCodeConstant.EVALUATION_ALREADY_DELETED, message);
    }
}