package com.medireserve.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的评价返回 VO
 * 患者查看自己提交的评价列表
 */
@Data
public class MyEvaluationVO {

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
     * 评分
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
     * 评价状态（1-已发布，2-已删除）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}