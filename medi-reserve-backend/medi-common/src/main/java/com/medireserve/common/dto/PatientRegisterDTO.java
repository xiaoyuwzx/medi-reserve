package com.medireserve.common.dto;

import com.medireserve.common.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 患者注册请求DTO
 */
@Data
@Schema(description = "患者注册请求 DTO")
public class PatientRegisterDTO {

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

    @Schema(description = "身份证号（18位）")
    @Pattern(regexp = "^[1-9]\\d{16}[0-9Xx]$", message = MessageConstant.VALIDATION_ID_CARD_FORMAT)
    private String idCard;

    @Schema(description = "性别：0=未知，1=男，2=女")
    private Integer gender;
}