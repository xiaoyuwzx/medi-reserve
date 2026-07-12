package com.medireserve.common.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 医生列表查询请求DTO
 * 患者端 GET /patient/doctors 接收的参数
 */
@Data
public class DoctorListQueryDTO {

    /**
     * 科室筛选（精确匹配）
     * 可选，不传则查询所有科室
     */
    private String department;

    /**
     * 关键词搜索（姓名 或 擅长领域 模糊匹配）
     * 可选，不传则不过滤关键词
     */
    private String keyword;

    /**
     * 页码（从1开始），默认1
     */
    @Min(value = 1, message = "页码至少为1")
    private Integer page = 1;

    /**
     * 每页条数，默认10，最大100
     */
    @Min(value = 1, message = "每页条数至少为1")
    private Integer size = 10;
}