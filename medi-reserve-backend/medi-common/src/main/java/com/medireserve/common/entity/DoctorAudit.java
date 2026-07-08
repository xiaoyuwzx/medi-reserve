package com.medireserve.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DoctorAudit {
    private Long id;
    private Long doctorId;//医生ID
    private String certificateUrl;// 执业证书URL
    private String qualificationUrl;// 资格证URL
    private String specialty;// 擅长领域
    private String introduction;// 个人简介
    private String avatar;//头像URL
    private Integer auditStatus;  // 审核状态：0待审核 1通过 2驳回
    private String auditRemark;//审核备注（驳回时记录原因）
    private LocalDateTime auditTime;//审核时间
    private Long auditorId;        // 审核人（管理员ID）
    private LocalDateTime createdAt;//注册时间
    private LocalDateTime updatedAt;//更新时间
}