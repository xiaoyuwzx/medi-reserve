package com.medireserve.common.dto;


import com.medireserve.common.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 医生注册请求DTO
 * 包含主表（doctor）和审核资料表（doctor_audit）的基础字段
 */
@Data
@Schema(description = "医生注册请求 DTO（含审核资料）")
public class DoctorRegisterDTO {

    // ========== 必填字段 ==========
    @Schema(description = "姓名", required = true)
    @NotBlank(message = MessageConstant.VALIDATION_NAME_NOT_EMPTY)
    private String name;

    @Schema(description = "手机号（11位）", required = true)
    @NotBlank(message = MessageConstant.VALIDATION_PHONE_NOT_EMPTY)
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = MessageConstant.VALIDATION_PHONE_FORMAT)
    private String phone;

    @Schema(description = "密码（6-20位字母和数字组合）", required = true)
    @NotBlank(message = MessageConstant.VALIDATION_PASSWORD_NOT_EMPTY)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$",
            message = MessageConstant.VALIDATION_PASSWORD_FORMAT)
    private String password;

    @Schema(description = "科室ID", required = true)
    @NotNull(message = MessageConstant.VALIDATION_DEPARTMENT_NOT_EMPTY)
    private Long departmentId;

    @Schema(description = "职称ID", required = true)
    @NotNull(message = MessageConstant.VALIDATION_TITLE_NOT_EMPTY)
    private Long titleId;

    // ========== 可选字段 ==========
    @Schema(description = "身份证号（18位）")
    @Pattern(regexp = "^[1-9]\\d{16}[0-9Xx]$", message = MessageConstant.VALIDATION_ID_CARD_FORMAT)
    private String idCard;

    @Schema(description = "性别：0=未知，1=男，2=女")
    @Min(value = 0, message = "性别参数错误（0=未知，1=男，2=女）")
    @Max(value = 2, message = "性别参数错误（0=未知，1=男，2=女）")
    private Integer gender;

    // ========== 审核资料字段（后续使用） ==========
    @Schema(description = "擅长领域")
    private String specialty;

    @Schema(description = "个人简介")
    private String introduction;
}