package com.medireserve.common.exception;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;

public class DepartmentNotFoundException extends BusinessException {
    public DepartmentNotFoundException() {
        super(StatusCodeConstant.DEPARTMENT_NOT_FOUND, MessageConstant.DEPARTMENT_NOT_FOUND);
    }
}