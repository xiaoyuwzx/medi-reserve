package com.medireserve.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 热门医生排行返回 VO
 * 用于患者端首页展示热门医生
 */
@Data
@Builder
public class DoctorHotVO {

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
     * 职称名称
     */
    private String titleName;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 热度综合评分（保留2位小数）
     */
    private Double hotScore;

    /**
     * 评价总数
     */
    private Integer evaluationCount;

}