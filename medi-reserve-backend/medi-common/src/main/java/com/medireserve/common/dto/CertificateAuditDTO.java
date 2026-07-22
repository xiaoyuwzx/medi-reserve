package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 证件审核请求 DTO（管理端）
 */
@Data
@Schema(description = "证件审核请求")
public class CertificateAuditDTO {

    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果：1-通过 2-驳回", allowableValues = {"1", "2"})
    private Integer result;

    @Schema(description = "驳回原因（驳回时必填）")
    private String remark;
}