package com.medireserve.common.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班实体类
 * 对应数据库表 schedule
 */
@Data
public class Schedule {
    private Long id;                // 排班ID（自增主键）
    private Long doctorId;          // 医生ID（关联 doctor 表）
    private LocalDate scheduleDate; // 排班日期（年-月-日）
    private Integer period;         // 时段：1=上午，2=下午
    private Integer maxCount;       // 最大挂号数（医生设定的上限）
    private Integer remainingCount; // 剩余号源（实时扣减后的剩余数量）
    private Integer status;         // 状态：1正常 2停诊 3已满
    private LocalDateTime createdAt; // 创建时间（自动生成）
    private LocalDateTime updatedAt; // 更新时间（自动更新）
}