package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 评分/好评率趋势数据 VO
 */
@Data
@Schema(description = "评分/好评率趋势数据")
public class RateTrendVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "当日平均评分（保留2位小数）")
    private BigDecimal avgScore;

    @Schema(description = "当日好评率（百分比，保留1位小数）")
    private BigDecimal positiveRate;

    @Schema(description = "当日评价总数")
    private Integer count;
}