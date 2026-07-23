package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 医生列表返回对象（患者端）
 * 用于患者浏览医生列表时展示
 */
@Data
@Schema(description = "医生列表返回对象（患者端）")
public class DoctorListVO {

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String name;

    @Schema(description = "科室名称")
    private String department;

    @Schema(description = "职称名称")
    private String title;

    @Schema(description = "擅长领域")
    private String specialty;

    @Schema(description = "职称权重（4=主任医师，1=住院医师）")
    private Integer titleWeight;

    @Schema(description = "未来7天是否有可用号源")
    private Boolean hasAvailableSlot;
}