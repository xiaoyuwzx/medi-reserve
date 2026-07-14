package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 评价不存在异常
 * 当删除或查询不存在的评价时抛出
 */
public class EvaluationNotFoundException extends BusinessException {

    public EvaluationNotFoundException() {
        super(StatusCodeConstant.EVALUATION_NOT_FOUND, MessageConstant.EVALUATION_NOT_FOUND);
    }

    public EvaluationNotFoundException(String message) {
        super(StatusCodeConstant.EVALUATION_NOT_FOUND, message);
    }
}