package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色权限更新请求 DTO
 */
@Data
@Schema(description = "角色权限更新请求 DTO")
public class RolePermissionUpdateDTO {

    @Schema(description = "权限ID列表", required = true)
    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
}