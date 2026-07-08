package com.medireserve.common.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Min(value = 1, message = "状态参数错误，只能为 1（正常）、2（停诊）或 3（已满）")
    @Max(value = 3, message = "状态参数错误，只能为 1（正常）、2（停诊）或 3（已满）")
    private Integer status;  // 1-正常，2-停诊，3-已满

    // 自定义校验：结束日期不能早于开始日期
    @AssertTrue(message = "结束日期不能早于开始日期")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }

}