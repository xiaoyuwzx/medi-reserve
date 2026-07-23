package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 热门医生排行返回 VO
 * 用于患者端首页展示热门医生
 */
@Data
@Builder
@Schema(description = "热门医生排行返回 VO")
public class DoctorHotVO {

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "职称名称")
    private String titleName;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "热度综合评分（保留2位小数）")
    private Double hotScore;

    @Schema(description = "评价总数")
    private Integer evaluationCount;
}