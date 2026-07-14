package com.medireserve.common.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 评价列表返回 VO
 * 用于患者端评价列表展示
 */
@Data
public class EvaluationListVO {

    /**
     * 评价ID
     */
    private Long evaluationId;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 科室名称
     */
    private String departmentName;

    /**
     * 排班日期（就诊日期）
     */
    private LocalDate scheduleDate;

    /**
     * 评分（1-5）
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous;

    /**
     * 评价时间
     */
    private LocalDateTime createdAt;

}