package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 评价创建失败异常
 * 当数据库插入影响行数为0时抛出
 */
public class EvaluationCreateFailedException extends BusinessException {

    public EvaluationCreateFailedException() {
        super(StatusCodeConstant.EVALUATION_CREATE_FAILED, MessageConstant.EVALUATION_CREATE_FAILED);
    }

    public EvaluationCreateFailedException(String message) {
        super(StatusCodeConstant.EVALUATION_CREATE_FAILED, message);
    }
}