package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 状态分布 VO
 */
@Data
@Schema(description = "预约状态分布 VO")
public class StatusDistributionVO {

    @Schema(description = "状态码：0-待支付，1-已支付，2-已就诊，3-已取消，4-已过期")
    private Integer status;

    @Schema(description = "状态描述")
    private String label;

    @Schema(description = "数量")
    private Long count;
}