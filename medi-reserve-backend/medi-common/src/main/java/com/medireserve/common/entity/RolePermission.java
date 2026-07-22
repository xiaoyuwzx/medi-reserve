package com.medireserve.common.entity;

import lombok.Data;

/**
 * 角色权限关联实体
 * 对应数据库表 role_permission
 */
@Data
public class RolePermission {
    private Integer roleId;
    private Long permissionId;
}