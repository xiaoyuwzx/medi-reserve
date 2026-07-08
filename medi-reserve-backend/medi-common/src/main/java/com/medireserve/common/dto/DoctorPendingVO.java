package com.medireserve.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待审核医生列表返回对象
 * 用于管理员查看待审核医生列表
 */
@Data
public class DoctorPendingVO {

    // ========== doctor 表字段 ==========
    private Long doctorId;          // 医生ID
    private String name;            // 姓名
    private String phone;           // 手机号
    private String department;      // 科室
    private String title;           // 职称
    private LocalDateTime createdAt; // 注册时间

    // ========== doctor_audit 表字段 ==========
    private String specialty;       // 擅长领域
    private String introduction;    // 个人简介
    private String certificateUrl;  // 执业证书URL
    private String qualificationUrl; // 资格证URL
}