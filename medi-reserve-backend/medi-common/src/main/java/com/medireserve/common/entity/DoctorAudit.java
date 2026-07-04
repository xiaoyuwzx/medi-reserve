package com.medireserve.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DoctorAudit {
    private Long id;
    private Long doctorId;
    private String certificateUrl;
    private String qualificationUrl;
    private String specialty;
    private String introduction;
    private String avatar;
    private Integer auditStatus;  // 0待审核 1通过 2驳回
    private String auditRemark;
    private LocalDateTime auditTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}