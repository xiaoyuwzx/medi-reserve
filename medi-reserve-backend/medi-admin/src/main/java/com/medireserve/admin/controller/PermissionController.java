package com.medireserve.admin.controller;

import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.PermissionNodeVO;
import com.medireserve.common.dto.RolePermissionUpdateDTO;
import com.medireserve.common.dto.RoleVO;
import com.medireserve.common.result.Result;
import com.medireserve.common.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理接口
 * 仅超级管理员可访问
 */
@Slf4j
@RestController
@RequestMapping("/admin/permissions")
@Tag(name = "管理端 - 权限管理", description = "权限树、角色权限分配")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取权限树
     * @return
     */
    @GetMapping("/tree")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "获取权限树")
    public Result<List<PermissionNodeVO>> getPermissionTree() {

        log.info("获取权限树");

        List<PermissionNodeVO> tree = permissionService.getPermissionTree();

        return Result.success(tree);

    }

    /**
     * 查询所有角色及其权限
     * @return
     */
    @GetMapping("/roles")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "查询所有角色及其权限")
    public Result<List<RoleVO>> getAllRolesWithPermissions() {

        log.info("查询所有角色及其权限");

        List<RoleVO> roles = permissionService.getAllRolesWithPermissions();

        return Result.success(roles);

    }

    /**
     * 查询指定角色拥有的权限ID列表
     * @param roleId
     * @return
     */
    @GetMapping("/roles/{roleId}/permissions")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "查询角色拥有的权限ID列表")
    public Result<List<Long>> getRolePermissionIds(
            @Parameter(description = "角色ID") @PathVariable Integer roleId) {

        log.info("查询角色权限，角色ID：{}", roleId);

        List<Long> ids = permissionService.getPermissionIdsByRoleId(roleId);

        return Result.success(ids);

    }

    /**
     * 更新角色权限（全量覆盖）
     * @param roleId
     * @param dto
     * @return
     */
    @PutMapping("/roles/{roleId}/permissions")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "更新角色权限")
    @LogOperation(module = "权限管理", operation = "更新角色权限")
    public Result<Void> updateRolePermissions(
            @Parameter(description = "角色ID") @PathVariable Integer roleId,
            @RequestBody @Valid RolePermissionUpdateDTO dto) {

        log.info("更新角色权限，角色ID：{}，权限数：{}", roleId, dto.getPermissionIds().size());

        permissionService.updateRolePermissions(roleId, dto);

        return Result.success(MessageConstant.PERMISSION_UPDATE_SUCCESS, null);

    }

}
