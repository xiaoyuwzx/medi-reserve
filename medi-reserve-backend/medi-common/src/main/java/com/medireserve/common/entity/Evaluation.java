package com.medireserve.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 就诊评价实体类
 * 对应数据库表 evaluation
 */
@Data
public class Evaluation {

    /**
     * 评价ID（主键自增）
     */
    private Long id;

    /**
     * 预约ID（关联 appointment 表）
     * 唯一约束：一个预约只能评价一次
     */
    private Long appointmentId;

    /**
     * 患者ID（评价人）
     */
    private Long patientId;

    /**
     * 医生ID（被评价人）
     */
    private Long doctorId;

    /**
     * 排班ID（冗余字段，方便查询时关联排班日期）
     */
    private Long scheduleId;

    /**
     * 评分：1-5星
     */
    private Integer score;

    /**
     * 评价内容（选填，最多500字）
     */
    private String content;

    /**
     * 是否匿名：0-不匿名，1-匿名
     */
    private Integer isAnonymous;

    /**
     * 状态：1-已发布，2-已隐藏
     * 使用 EvaluationStatusConstant 常量
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}