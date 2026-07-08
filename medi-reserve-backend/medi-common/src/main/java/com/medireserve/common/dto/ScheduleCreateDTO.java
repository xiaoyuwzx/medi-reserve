package com.medireserve.common.dto;

import com.medireserve.common.constant.MessageConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 新增排班请求DTO
 * 医生端 POST /doctor/schedules 接收的参数
 */
@Data
public class ScheduleCreateDTO {

    /*@NotNull(message = "医生ID不能为空")
    private Long doctorId;*/

    @NotNull(message = "排班日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    @NotNull(message = "时段不能为空")
    @Min(value = 1, message = "时段参数错误（1=上午，2=下午）")
    @Max(value = 2, message = "时段参数错误（1=上午，2=下午）")
    private Integer period;

    @NotNull(message = "最大挂号数不能为空")
    @Min(value = 1, message = "最大挂号数至少为1")
    @Max(value = 100, message = "最大挂号数不能超过100")
    private Integer maxCount;
}