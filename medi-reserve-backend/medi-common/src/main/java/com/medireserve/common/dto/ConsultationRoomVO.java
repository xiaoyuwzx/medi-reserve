package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 问诊室信息返回 VO
 * 用于进入问诊页面时展示患者/医生信息及状态
 */
@Data
@Builder
@Schema(description = "问诊室信息返回 VO")
public class ConsultationRoomVO {

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "排班日期")
    private String scheduleDate;

    @Schema(description = "问诊状态：1-进行中，0-已结束")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "当前在线人数")
    private Integer onlineCount;
}