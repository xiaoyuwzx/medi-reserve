package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 操作日志查询条件 DTO
 */
@Data
@Schema(description = "操作日志查询条件 DTO")
public class OperationLogQueryDTO {

    @Schema(description = "操作管理员ID（精确查询）")
    private Long adminId;

    @Schema(description = "操作模块（如：审核管理）")
    private String module;

    @Schema(description = "操作结果：1-成功，0-失败")
    private Integer result;

    @Schema(description = "开始日期（yyyy-MM-dd）")
    private LocalDate startDate;

    @Schema(description = "结束日期（yyyy-MM-dd）")
    private LocalDate endDate;

    @Schema(description = "页码（从1开始）")
    private Integer pageNum = 1;

    @Schema(description = "每页大小（默认10）")
    private Integer pageSize = 10;
}