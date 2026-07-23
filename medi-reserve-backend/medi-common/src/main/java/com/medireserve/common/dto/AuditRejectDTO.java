package com.medireserve.common.dto;

import com.medireserve.common.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核驳回请求DTO
 * 管理员驳回医生申请时传入
 */
@Data
@Schema(description = "审核驳回请求 DTO")
public class AuditRejectDTO {

    @Schema(description = "驳回原因", required = true)
    @NotBlank(message = MessageConstant.DOCTOR_AUDIT_REJECT_REASON_EMPTY)
    private String rejectReason;
}