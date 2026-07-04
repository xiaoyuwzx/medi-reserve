package com.medireserve.common.dto;

import com.medireserve.common.constant.MessageConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通用登录请求DTO
 * 患者/医生使用 phone 登录，管理员使用 username 登录
 */
@Data
public class LoginDTO {

    @NotBlank(message = MessageConstant.VALIDATION_ACCOUNT_NOT_EMPTY)
    private String username;  // 患者/医生存手机号，管理员存用户名

    @NotBlank(message = MessageConstant.VALIDATION_PASSWORD_NOT_EMPTY)
    private String password;
}