package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 角色 VO
 */
@Data
@Schema(description = "角色信息")
public class RoleVO {
    @Schema(description = "角色ID")
    private Integer id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "拥有的权限ID列表")
    private List<Long> permissionIds;
}