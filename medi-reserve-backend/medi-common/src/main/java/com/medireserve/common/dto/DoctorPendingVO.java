package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待审核医生列表返回对象
 * 用于管理员查看待审核医生列表
 */
@Data
@Schema(description = "待审核医生列表返回 VO（管理端）")
public class DoctorPendingVO {

    // ========== doctor 表字段 ==========
    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "职称名称")
    private String titleName;

    @Schema(description = "注册时间")
    private LocalDateTime createdAt;

    // ========== doctor_audit 表字段 ==========
    @Schema(description = "擅长领域")
    private String specialty;

    @Schema(description = "个人简介")
    private String introduction;

    @Schema(description = "执业证书URL")
    private String certificateUrl;

    @Schema(description = "资格证URL")
    private String qualificationUrl;
}