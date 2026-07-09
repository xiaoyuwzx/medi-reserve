package com.medireserve.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建预约（下单）请求DTO
 * 患者端 POST /patient/appointments 接收的参数
 */
@Data
public class AppointmentCreateDTO {

    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;   // 患者选择的排班ID
}