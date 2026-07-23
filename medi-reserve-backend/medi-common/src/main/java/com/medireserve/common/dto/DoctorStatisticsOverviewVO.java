package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生端统计总览 VO
 * 位置：medi-common/src/main/java/com/medireserve/common/dto/DoctorStatisticsOverviewVO.java
 */
@Data
@Schema(description = "医生端统计总览数据 VO")
public class DoctorStatisticsOverviewVO {

    @Schema(description = "总接诊人数（已支付+已完成）")
    private Long totalPatients;

    @Schema(description = "今日接诊人数（已支付+已完成）")
    private Long todayPatients;

    @Schema(description = "平均评分（0-5，保留2位）")
    private BigDecimal avgScore;

    @Schema(description = "好评率（评分>=4的比例，百分比）")
    private BigDecimal positiveRate;

    @Schema(description = "评价总数")
    private Long evaluationCount;

    @Schema(description = "待处理问诊数（已支付未就诊）")
    private Long pendingConsultations;
}