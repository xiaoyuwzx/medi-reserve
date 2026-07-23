package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "排班查询请求 DTO")
public class ScheduleQueryDTO {

    @Schema(description = "查询开始日期（yyyy-MM-dd）")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "查询结束日期（yyyy-MM-dd）")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "排班状态：1-正常，2-停诊，3-已满")
    @Min(value = 1, message = "状态参数错误，只能为 1（正常）、2（停诊）或 3（已满）")
    @Max(value = 3, message = "状态参数错误，只能为 1（正常）、2（停诊）或 3（已满）")
    private Integer status;

    @AssertTrue(message = "结束日期不能早于开始日期")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }
}