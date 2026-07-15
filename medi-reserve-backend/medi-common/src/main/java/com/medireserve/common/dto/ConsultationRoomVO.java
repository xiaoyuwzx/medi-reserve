package com.medireserve.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 问诊室信息返回 VO
 * 用于进入问诊页面时展示患者/医生信息及状态
 */
@Data
@Builder
public class ConsultationRoomVO {

    /**
     * 预约ID
     */
    private Long appointmentId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

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
     * 排班日期
     */
    private String scheduleDate;

    /**
     * 问诊状态：1-进行中，0-已结束
     */
    private Integer status;

    /**
     * 状态文本描述
     */
    private String statusText;

    /**
     * 当前在线人数（实时从 Redis 获取）
     */
    private Integer onlineCount;
}