package com.medireserve.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色实体
 * 对应数据库表 role
 */
@Data
public class Role {
    private Integer id;             // 角色ID（与 admin.role 字段对应）
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}