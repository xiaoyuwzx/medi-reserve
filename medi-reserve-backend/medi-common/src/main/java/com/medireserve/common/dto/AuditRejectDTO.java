package com.medireserve.common.dto;

import com.medireserve.common.constant.MessageConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核驳回请求DTO
 * 管理员驳回医生申请时传入
 */
@Data
public class AuditRejectDTO {

    @NotBlank(message = MessageConstant.DOCTOR_AUDIT_REJECT_REASON_EMPTY)
    private String rejectReason;    // 驳回原因

}