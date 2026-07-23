package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 医生列表查询请求DTO
 * 患者端 GET /patient/doctors 接收的参数
 */
@Data
@Schema(description = "医生列表查询请求 DTO")
public class DoctorListQueryDTO {

    @Schema(description = "科室筛选（精确匹配）")
    private String department;

    @Schema(description = "关键词搜索（姓名/擅长领域）")
    private String keyword;

    @Schema(description = "页码（从1开始）")
    @Min(value = 1, message = "页码至少为1")
    private Integer page = 1;

    @Schema(description = "每页条数（1-100）")
    @Min(value = 1, message = "每页条数至少为1")
    private Integer size = 10;
}