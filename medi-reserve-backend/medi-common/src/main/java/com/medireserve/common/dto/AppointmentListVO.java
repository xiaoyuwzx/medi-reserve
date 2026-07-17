package com.medireserve.common.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 我的预约列表返回 VO
 * 用于患者端查看预约记录（含关联的医生、排班信息）
 */
@Data
public class AppointmentListVO {

    private Long id;                // 预约ID
    private String appointmentNo;   // 预约单号
    private Long scheduleId;        // 排班ID
    private Long patientId;         // 患者ID
    private Long doctorId;          // 医生ID
    private Integer status;         // 预约状态：0-待支付 1-已支付 2-已就诊 3-已取消 4-已过期
    private LocalDateTime createdAt; // 创建时间

    // ===== 关联信息 =====
    private String doctorName;      // 医生姓名
    private String departmentName;  // 科室名称
    private String titleName;       // 职称名称
    private LocalDate scheduleDate; // 就诊日期
    private Integer period;         // 时段：1上午 2下午
    private String periodText;      // 时段文本
}