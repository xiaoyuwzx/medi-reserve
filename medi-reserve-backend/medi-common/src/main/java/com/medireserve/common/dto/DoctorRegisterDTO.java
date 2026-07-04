package com.medireserve.common.dto;

import com.medireserve.common.constant.MessageConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 医生注册请求DTO
 * 包含主表（doctor）和审核资料表（doctor_audit）的基础字段
 */
@Data
public class DoctorRegisterDTO {

    // ========== 必填字段 ==========
    @NotBlank(message = MessageConstant.VALIDATION_NAME_NOT_EMPTY)
    private String name;

    @NotBlank(message = MessageConstant.VALIDATION_PHONE_NOT_EMPTY)
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = MessageConstant.VALIDATION_PHONE_FORMAT)
    private String phone;

    @NotBlank(message = MessageConstant.VALIDATION_PASSWORD_NOT_EMPTY)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$",
            message = MessageConstant.VALIDATION_PASSWORD_FORMAT)
    private String password;

    @NotBlank(message = MessageConstant.VALIDATION_DEPARTMENT_NOT_EMPTY)
    private String department;

    @NotBlank(message = MessageConstant.VALIDATION_TITLE_NOT_EMPTY)
    private String title;

    // ========== 可选字段 ==========
    @Pattern(regexp = "^[1-9]\\d{16}[0-9Xx]$", message = MessageConstant.VALIDATION_ID_CARD_FORMAT)
    private String idCard;

    private Integer gender;

    // ========== 审核资料字段（后续使用） ==========
    private String specialty;
    private String introduction;
}