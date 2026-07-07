package com.medireserve.common.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 排班查询请求DTO
 * 医生端 GET /doctor/schedules 接收的参数（可选，用于筛选）
 */
@Data
public class ScheduleQueryDTO {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;  // 查询开始日期（可选）

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;    // 查询结束日期（可选）
}