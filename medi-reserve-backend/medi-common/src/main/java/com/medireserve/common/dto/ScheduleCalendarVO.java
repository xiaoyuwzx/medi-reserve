package com.medireserve.common.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 排班日历返回对象（患者端）
 * 用于展示某医生未来7天的排班情况
 */
@Data
public class ScheduleCalendarVO {

    /**
     * 排班ID
     */
    private Long scheduleId;

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 时段：1上午 2下午
     */
    private Integer period;

    /**
     * 时段文本（上午/下午）
     */
    private String periodText;

    /**
     * 剩余号源
     */
    private Integer remainingCount;

    /**
     * 排班状态：1正常 2停诊 3已满
     */
    private Integer status;

    /**
     * 状态文本（正常/停诊/已满）
     */
    private String statusText;
}