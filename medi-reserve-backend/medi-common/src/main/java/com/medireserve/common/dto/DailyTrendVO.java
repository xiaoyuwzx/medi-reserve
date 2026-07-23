package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 每日趋势数据点
 * 位置：medi-common/src/main/java/com/medireserve/common/dto/DailyTrendVO.java
 */
@Data
@Schema(description = "每日趋势数据")
public class DailyTrendVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "当日接诊量")
    private Long count;
}