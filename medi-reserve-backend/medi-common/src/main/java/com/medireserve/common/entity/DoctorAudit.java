package com.medireserve.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DoctorAudit {

    private Long id;
    private Long doctorId;//医生ID

    // ===== 已审核通过的有效证件 =====
    private String certificateUrl;// 执业证书URL
    private String qualificationUrl;// 资格证URL

    // ===== 待审核的新证件 =====
    private String pendingCertificateUrl;
    private String pendingQualificationUrl;

    // ===== 审核状态 =====
    private Integer certAuditStatus;    // 0-待审核 1-已通过 2-已驳回
    private String certAuditRemark;
    private LocalDateTime certAuditTime;
    private Long certAuditorId;

    // ===== 专业信息 =====
    private String specialty;// 擅长领域
    private String introduction;// 个人简介
    private String avatar;//头像URL

    // ===== 首次注册审核（原有字段） =====
    private Integer auditStatus;  // 审核状态：0待审核 1通过 2驳回
    private String auditRemark;//审核备注（驳回时记录原因）
    private LocalDateTime auditTime;//审核时间
    private Long auditorId;        // 审核人（管理员ID）

    private LocalDateTime createdAt;//注册时间
    private LocalDateTime updatedAt;//更新时间

}