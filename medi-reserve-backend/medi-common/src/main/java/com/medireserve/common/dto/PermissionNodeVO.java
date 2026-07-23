package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 权限树节点 VO
 */
@Data
@Schema(description = "权限树节点 VO")
public class PermissionNodeVO {

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "权限代码（如 admin:audit:view）")
    private String code;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "类型：1-菜单，2-按钮，3-接口")
    private Integer type;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "子权限列表")
    private List<PermissionNodeVO> children;
}