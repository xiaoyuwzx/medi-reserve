package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 评价列表返回 VO
 * 用于患者端评价列表展示
 */
@Data
@Schema(description = "医生评价列表返回 VO（患者端查看医生评价）")
public class EvaluationListVO {

    @Schema(description = "评价ID")
    private Long evaluationId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "排班日期（就诊日期）")
    private LocalDate scheduleDate;

    @Schema(description = "评分（1-5）")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "是否匿名")
    private Boolean isAnonymous;

    @Schema(description = "评价时间")
    private LocalDateTime createdAt;
}