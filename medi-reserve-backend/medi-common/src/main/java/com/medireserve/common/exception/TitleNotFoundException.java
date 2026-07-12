package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

public class TitleNotFoundException extends BusinessException {
    public TitleNotFoundException() {
        super(StatusCodeConstant.TITLE_NOT_FOUND, MessageConstant.TITLE_NOT_FOUND);
    }
}