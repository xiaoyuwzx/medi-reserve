package com.medireserve.common.dto;

import com.medireserve.common.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理员注册请求DTO
 */
@Data
@Schema(description = "管理员注册请求 DTO")
public class AdminRegisterDTO {

    @Schema(description = "用户名（4-20位字母数字下划线）", required = true)
    @NotBlank(message = MessageConstant.VALIDATION_ACCOUNT_NOT_EMPTY)
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = MessageConstant.VALIDATION_USERNAME_FORMAT)
    private String username;

    @Schema(description = "密码（6-20位字母和数字组合）", required = true)
    @NotBlank(message = MessageConstant.VALIDATION_PASSWORD_NOT_EMPTY)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$",
            message = MessageConstant.VALIDATION_PASSWORD_FORMAT)
    private String password;

    @Schema(description = "真实姓名", required = true)
    @NotBlank(message = MessageConstant.VALIDATION_NAME_NOT_EMPTY)
    private String name;

    @Schema(description = "手机号（11位）", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = MessageConstant.VALIDATION_PHONE_FORMAT)
    private String phone;

    @Schema(description = "邮箱", example = "admin@example.com")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = MessageConstant.VALIDATION_EMAIL_FORMAT)
    private String email;

    @Schema(description = "角色（1=超级管理员，2=普通管理员），默认普通管理员")
    private Integer role;
}