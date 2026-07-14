package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

/**
 * 评价删除失败异常
 * 当数据库更新影响行数为0时抛出
 */
public class EvaluationDeleteFailedException extends BusinessException {

    public EvaluationDeleteFailedException() {
        super(StatusCodeConstant.EVALUATION_DELETE_FAILED, MessageConstant.EVALUATION_DELETE_FAILED);
    }

    public EvaluationDeleteFailedException(String message) {
        super(StatusCodeConstant.EVALUATION_DELETE_FAILED, message);
    }
}