package com.medireserve.common.dto;

import lombok.Data;

/**
 * 医生列表返回对象（患者端）
 * 用于患者浏览医生列表时展示
 */
@Data
public class DoctorListVO {

    /**
     * 医生ID（用于后续查询排班）
     */
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String name;

    /**
     * 科室
     */
    private String department;

    /**
     * 职称（主任医师/副主任医师/主治医师/住院医师）
     */
    private String title;

    /**
     * 擅长领域
     */
    private String specialty;

    /**
     * 职称权重（用于排序，数值越大职称越高）
     * 主任医师=4，副主任医师=3，主治医师=2，住院医师=1
     */
    private Integer titleWeight;

    /**
     * 是否有可用号源（未来7天有剩余号源）
     */
    private Boolean hasAvailableSlot;
}