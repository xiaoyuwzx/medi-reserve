package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 趋势数据点
 * 位置：medi-common/src/main/java/com/medireserve/common/dto/TrendDataVO.java
 */
@Data
@Schema(description = "每日趋势数据")
public class TrendDataVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "当日挂号量")
    private Long appointments;

    @Schema(description = "当日支付量")
    private Long paid;

    @Schema(description = "当日收入（估计值）")
    private BigDecimal income;
}