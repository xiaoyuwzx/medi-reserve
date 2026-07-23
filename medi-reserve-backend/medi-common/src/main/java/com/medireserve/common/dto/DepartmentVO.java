package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "科室列表返回对象")
public class DepartmentVO {

    @Schema(description = "科室ID")
    private Long id;

    @Schema(description = "科室名称")
    private String department;

    @Schema(description = "该科室可挂号医生数量")
    private Integer doctorCount;
}