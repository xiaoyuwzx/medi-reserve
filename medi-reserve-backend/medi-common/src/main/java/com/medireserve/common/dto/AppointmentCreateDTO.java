package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建预约请求 DTO")
public class AppointmentCreateDTO {

    @Schema(description = "排班ID", required = true)
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;
}