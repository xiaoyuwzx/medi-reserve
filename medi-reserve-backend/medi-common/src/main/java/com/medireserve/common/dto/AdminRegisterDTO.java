package com.medireserve.common.dto;

import com.medireserve.common.constant.MessageConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理员注册请求DTO
 */
@Data
public class AdminRegisterDTO {

    @NotBlank(message = MessageConstant.VALIDATION_ACCOUNT_NOT_EMPTY)
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = MessageConstant.VALIDATION_USERNAME_FORMAT)
    private String username;

    @NotBlank(message = MessageConstant.VALIDATION_PASSWORD_NOT_EMPTY)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$",
            message = MessageConstant.VALIDATION_PASSWORD_FORMAT)
    private String password;

    @NotBlank(message = MessageConstant.VALIDATION_NAME_NOT_EMPTY)
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = MessageConstant.VALIDATION_PHONE_FORMAT)
    private String phone;

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = MessageConstant.VALIDATION_EMAIL_FORMAT)
    private String email;

    private Integer role;
}