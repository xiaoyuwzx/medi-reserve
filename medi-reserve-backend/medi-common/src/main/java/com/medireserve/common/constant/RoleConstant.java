package com.medireserve.common.constant;

/**
 * 角色常量
 */
public class RoleConstant {

    // ========== 角色名称（存储在JWT中） ==========
    public static final String PATIENT = "PATIENT";   // 患者
    public static final String DOCTOR = "DOCTOR";     // 医生
    public static final String ADMIN = "ADMIN";       // 管理员

    // ========== 管理员角色（存储于 admin.role 字段） ==========
    public static final Integer ADMIN_SUPER = 1;      // 超级管理员
    public static final Integer ADMIN_NORMAL = 2;     // 普通管理员
}