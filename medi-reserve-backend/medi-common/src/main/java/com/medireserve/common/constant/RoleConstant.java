package com.medireserve.common.constant;

import com.medireserve.common.exception.BusinessException;

/**
 * 角色常量
 */
public class RoleConstant {

    // ========== 角色名称（存储在JWT中） ==========
    public static final String PATIENT = "PATIENT";   // 患者
    public static final String DOCTOR = "DOCTOR";     // 医生
    public static final String SUPER_ADMIN = "SUPER_ADMIN";  // 超级管理员
    public static final String NORMAL_ADMIN = "NORMAL_ADMIN"; // 普通管理员

    // ========== 管理员角色（存储于 admin.role 字段） ==========
    public static final Integer ADMIN_SUPER = 1;      // 超级管理员
    public static final Integer ADMIN_NORMAL = 2;     // 普通管理员

    /**
     * 根据角色ID获取角色名称（用于 JWT 存储）
     */
    public static String getRoleName(Integer roleId) {
        if (ADMIN_SUPER.equals(roleId)) {
            return SUPER_ADMIN;
        } else if (ADMIN_NORMAL.equals(roleId)) {
            return NORMAL_ADMIN;
        }
        throw new BusinessException("未知的管理员角色: " + roleId);
    }

}