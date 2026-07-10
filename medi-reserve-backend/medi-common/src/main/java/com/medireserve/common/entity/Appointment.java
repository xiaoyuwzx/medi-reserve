package com.medireserve.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约记录实体类
 * 对应数据库表 appointment
 */
@Data
public class Appointment {
    private Long id;//预约ID（自增主键）
    private String appointmentNo;//预约单号
    private Long scheduleId;//排班ID（关联 schedule 表）
    private Long patientId;//患者ID（关联 patient 表）
    private Long doctorId;//医生ID（冗余字段，方便查询，避免连表）
    private Integer status;//预约状态  0-待支付，1-已支付，2-已就诊，3-已取消，4-已过期
    private LocalDateTime createdAt;//创建时间（下单时间）
    private LocalDateTime updatedAt;//更新时间（最后一次状态修改时间）
}