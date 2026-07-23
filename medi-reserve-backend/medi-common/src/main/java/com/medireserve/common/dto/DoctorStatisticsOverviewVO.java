package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生端统计总览 VO
 * 位置：medi-common/src/main/java/com/medireserve/common/dto/DoctorStatisticsOverviewVO.java
 */
@Data
@Schema(description = "医生端统计总览数据")
public class DoctorStatisticsOverviewVO {

    @Schema(description = "总接诊人数（已支付 + 已完成）")
    private Long totalPatients;

    @Schema(description = "今日接诊人数（已支付 + 已完成）")
    private Long todayPatients;

    @Schema(description = "平均评分（保留两位小数，若无评价则为 0）")
    private BigDecimal avgScore;

    @Schema(description = "好评率（评分 >= 4 的比例，百分比，如 85.5）")
    private BigDecimal positiveRate;

    @Schema(description = "评价总数")
    private Long evaluationCount;

    @Schema(description = "待处理问诊数（状态为已支付但未就诊，即未完成）")
    private Long pendingConsultations;
}