package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 医生个人信息更新请求 DTO
 * 普通信息（姓名、手机号等）立即生效，证件信息提交审核
 */
@Data
@Schema(description = "医生个人信息更新请求")
public class DoctorUpdateDTO {

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", required = true)
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true)
    private String phone;

    @Schema(description = "性别：0-未知 1-男 2-女")
    private Integer gender;

    @Pattern(regexp = "^[1-9]\\d{16}[0-9Xx]$", message = "身份证号格式不正确")
    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "擅长领域")
    private String specialty;

    @Schema(description = "个人简介")
    private String introduction;

    // ===== 证件字段（提交审核，不立即生效） =====
    @Schema(description = "新执业证书图片URL（提交审核，需管理员审批）")
    private String certificateUrl;

    @Schema(description = "新资格证图片URL（提交审核，需管理员审批）")
    private String qualificationUrl;
}