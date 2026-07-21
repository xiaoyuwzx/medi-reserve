package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 科室排行 VO
 * 位置：medi-common/src/main/java/com/medireserve/common/dto/DepartmentRankingVO.java
 */
@Data
@Schema(description = "科室排行数据")
public class DepartmentRankingVO {

    @Schema(description = "科室ID")
    private Long departmentId;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "预约总数")
    private Long appointmentCount;

    @Schema(description = "占比（百分比）")
    private BigDecimal ratio;
}