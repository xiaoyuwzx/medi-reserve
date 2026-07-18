package com.medireserve.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 科室列表返回对象
 * 用于患者端科室下拉选择框
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentVO {

    /**
     * 科室ID
     */
    private Long id;

    /**
     * 科室名称
     */
    private String department;

    /**
     * 该科室下可挂号医生数量（已审核通过的医生）
     */
    private Integer doctorCount;
}