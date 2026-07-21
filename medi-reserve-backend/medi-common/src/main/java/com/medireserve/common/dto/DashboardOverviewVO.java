package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端总览统计响应 VO
 * 位置：medi-common/src/main/java/com/medireserve/common/dto/DashboardOverviewVO.java
 */
@Data
@Schema(description = "总览统计数据")
public class DashboardOverviewVO {

    @Schema(description = "今日挂号总数（含待支付、已支付、已就诊）")
    private Long todayAppointments;

    @Schema(description = "今日已支付数")
    private Long todayPaid;

    @Schema(description = "今日收入（估计值，按固定单价计算）")
    private BigDecimal todayIncome;

    @Schema(description = "历史总挂号数（有效预约）")
    private Long totalAppointments;

    @Schema(description = "历史总收入（估计值）")
    private BigDecimal totalIncome;

    @Schema(description = "总患者数")
    private Long totalPatients;

    @Schema(description = "总医生数（审核通过且启用）")
    private Long totalDoctors;

    @Schema(description = "总评价数")
    private Long totalEvaluations;

    // 可额外增加昨日对比等字段，此处暂略
}