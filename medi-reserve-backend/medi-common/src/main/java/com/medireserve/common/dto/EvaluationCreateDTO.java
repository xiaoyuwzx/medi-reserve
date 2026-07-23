package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评价请求 DTO
 * 患者端 POST /patient/evaluations 接收的参数
 */
@Data
@Schema(description = "创建评价请求 DTO")
public class EvaluationCreateDTO {

    @Schema(description = "预约ID", required = true)
    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    @Schema(description = "评分（1-5）", required = true)
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分必须在1-5之间")
    @Max(value = 5, message = "评分必须在1-5之间")
    private Integer score;

    @Schema(description = "评价内容（最多500字）")
    @Size(max = 500, message = "评价内容不能超过500字")
    private String content;

    @Schema(description = "是否匿名（默认false）")
    private Boolean isAnonymous = false;
}