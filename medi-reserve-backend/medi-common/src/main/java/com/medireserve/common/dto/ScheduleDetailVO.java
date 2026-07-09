package com.medireserve.common.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班详情返回对象（挂号确认页展示）
 * GET /patient/schedules/{scheduleId} 的返回数据
 */
@Data
public class ScheduleDetailVO {

    private Long scheduleId;          // 排班ID
    private Long doctorId;            // 医生ID
    private String doctorName;        // 医生姓名
    private String department;        // 科室
    private String title;             // 职称
    private LocalDate scheduleDate;   // 排班日期
    private Integer period;           // 时段：1上午 2下午
    private String periodText;        // 时段文本（上午/下午）
    private Integer remainingCount;   // 剩余号源
    private Integer status;           // 排班状态：1正常 2停诊 3已满
    private String statusText;        // 状态文本
}