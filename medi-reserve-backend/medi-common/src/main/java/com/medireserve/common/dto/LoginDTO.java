package com.medireserve.common.dto;

import com.medireserve.common.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通用登录请求DTO
 * 患者/医生使用 phone 登录，管理员使用 username 登录
 */
@Data
@Schema(description = "通用登录请求 DTO（患者/医生使用手机号，管理员使用用户名）")
public class LoginDTO {

    @Schema(description = "登录账号（手机号或用户名）", required = true)
    @NotBlank(message = MessageConstant.VALIDATION_ACCOUNT_NOT_EMPTY)
    private String username;

    @Schema(description = "密码", required = true)
    @NotBlank(message = MessageConstant.VALIDATION_PASSWORD_NOT_EMPTY)
    private String password;
}