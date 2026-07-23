package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 我的预约列表返回 VO
 * 用于患者端查看预约记录（含关联的医生、排班信息）
 */
@Data
@Schema(description = "预约列表返回 VO（患者端/医生端通用）")
public class AppointmentListVO {

    @Schema(description = "预约ID")
    private Long id;

    @Schema(description = "预约单号")
    private String appointmentNo;

    @Schema(description = "排班ID")
    private Long scheduleId;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "预约状态：0-待支付，1-已支付，2-已就诊，3-已取消，4-已过期")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "职称名称")
    private String titleName;

    @Schema(description = "就诊日期")
    private LocalDate scheduleDate;

    @Schema(description = "时段：1=上午，2=下午")
    private Integer period;

    @Schema(description = "时段文本（上午/下午）")
    private String periodText;

    @Schema(description = "患者姓名（医生端查询）")
    private String patientName;

    @Schema(description = "患者手机号（医生端查询）")
    private String patientPhone;
}
