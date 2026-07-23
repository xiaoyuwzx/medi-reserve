package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的评价返回 VO
 * 患者查看自己提交的评价列表
 */
@Data
@Schema(description = "我的评价返回 VO（患者端查看自己的评价）")
public class MyEvaluationVO {

    @Schema(description = "评价ID")
    private Long evaluationId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "评分（1-5）")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "是否匿名")
    private Boolean isAnonymous;

    @Schema(description = "评价状态：1-已发布，2-已删除")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}