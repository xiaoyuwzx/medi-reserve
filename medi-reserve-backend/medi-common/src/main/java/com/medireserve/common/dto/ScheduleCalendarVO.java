package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 排班日历返回对象（患者端）
 * 用于展示某医生未来7天的排班情况
 */
@Data
@Schema(description = "排班日历返回 VO（患者端）")
public class ScheduleCalendarVO {

    @Schema(description = "排班ID")
    private Long scheduleId;

    @Schema(description = "排班日期")
    private LocalDate scheduleDate;

    @Schema(description = "时段：1=上午，2=下午")
    private Integer period;

    @Schema(description = "时段文本（上午/下午）")
    private String periodText;

    @Schema(description = "剩余号源")
    private Integer remainingCount;

    @Schema(description = "排班状态：1-正常，2-停诊，3-已满")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;
}