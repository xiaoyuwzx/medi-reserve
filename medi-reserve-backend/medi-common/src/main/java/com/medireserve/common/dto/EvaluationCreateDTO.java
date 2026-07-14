package com.medireserve.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评价请求 DTO
 * 患者端 POST /patient/evaluations 接收的参数
 */
@Data
public class EvaluationCreateDTO {

    /**
     * 预约ID（必填）
     * 通过预约ID关联到医生和就诊信息
     */
    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    /**
     * 评分（必填）
     * 范围：1-5
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分必须在1-5之间")
    @Max(value = 5, message = "评分必须在1-5之间")
    private Integer score;

    /**
     * 评价内容（选填）
     * 最多500字
     */
    @Size(max = 500, message = "评价内容不能超过500字")
    private String content;

    /**
     * 是否匿名（选填，默认 false）
     * true-匿名，false-不匿名
     */
    private Boolean isAnonymous = false;

}