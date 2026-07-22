package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待审核医生证件列表 VO（管理端）
 */
@Data
@Schema(description = "待审核医生证件信息")
public class PendingCertAuditVO {

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "职称名称")
    private String titleName;

    @Schema(description = "当前生效的执业证书URL")
    private String currentCertificateUrl;

    @Schema(description = "当前生效的资格证URL")
    private String currentQualificationUrl;

    @Schema(description = "待审核的执业证书URL（新上传）")
    private String pendingCertificateUrl;

    @Schema(description = "待审核的资格证URL（新上传）")
    private String pendingQualificationUrl;

    @Schema(description = "提交时间（申请变更时间）")
    private LocalDateTime submittedAt;
}