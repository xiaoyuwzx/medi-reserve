package com.medireserve.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 权限实体
 * 对应数据库表 permission
 */
@Data
public class Permission {
    private Long id;
    private Long parentId;          // 父权限ID，0表示顶级
    private String code;            // 权限代码，如 admin:audit:view
    private String name;            // 权限名称
    private Integer type;           // 类型：1-菜单 2-按钮 3-接口
    private Integer sortOrder;      // 排序
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}