package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员端 - 修改患者账号状态 DTO
 */
@Data
@Schema(description = "修改患者账号状态 DTO（管理端）")
public class AdminPatientStatusDTO {

    @Schema(description = "目标状态：0-禁用，1-启用", allowableValues = {"0", "1"})
    private Integer status;
}