package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生排行 VO
 * 位置：medi-common/src/main/java/com/medireserve/common/dto/DoctorRankingVO.java
 */
@Data
@Schema(description = "医生排行数据")
public class DoctorRankingVO {

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "预约总数")
    private Long appointmentCount;

    @Schema(description = "平均评分（5分制）")
    private BigDecimal avgScore;

    @Schema(description = "排名")
    private Integer rank;
}