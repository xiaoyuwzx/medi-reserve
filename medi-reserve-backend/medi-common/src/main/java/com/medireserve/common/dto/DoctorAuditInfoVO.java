package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生审核资料 VO（含证件审核状态）
 * 用于医生端查询自己的审核状态
 */
@Data
@Schema(description = "医生审核资料信息 VO（医生端查询审核状态）")
public class DoctorAuditInfoVO {

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "当前生效的执业证书URL")
    private String certificateUrl;

    @Schema(description = "当前生效的资格证URL")
    private String qualificationUrl;

    @Schema(description = "待审核的执业证书URL")
    private String pendingCertificateUrl;

    @Schema(description = "待审核的资格证URL")
    private String pendingQualificationUrl;

    @Schema(description = "证件审核状态：0-待审核，1-已通过，2-已驳回")
    private Integer certAuditStatus;

    @Schema(description = "审核状态描述")
    private String certAuditStatusText;

    @Schema(description = "审核备注（驳回原因）")
    private String certAuditRemark;

    @Schema(description = "审核时间")
    private LocalDateTime certAuditTime;

    @Schema(description = "擅长领域")
    private String specialty;

    @Schema(description = "个人简介")
    private String introduction;
}