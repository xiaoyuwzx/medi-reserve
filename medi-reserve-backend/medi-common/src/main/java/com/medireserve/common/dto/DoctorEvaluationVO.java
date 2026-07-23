package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生评价列表 VO（患者视角）
 */
@Data
@Schema(description = "医生收到的评价列表项 VO")
public class DoctorEvaluationVO {

    @Schema(description = "评价ID")
    private Long evaluationId;

    @Schema(description = "患者姓名（匿名时显示'匿名用户'）")
    private String patientName;

    @Schema(description = "评分（1-5）")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "是否匿名")
    private Boolean isAnonymous;

    @Schema(description = "评价时间")
    private LocalDateTime createdAt;
}