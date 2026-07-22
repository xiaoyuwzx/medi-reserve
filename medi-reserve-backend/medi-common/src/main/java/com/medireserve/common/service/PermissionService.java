package com.medireserve.common.service;

import com.medireserve.common.dto.PermissionNodeVO;
import com.medireserve.common.dto.RolePermissionUpdateDTO;
import com.medireserve.common.dto.RoleVO;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 获取权限树（用于前端展示）
     * @return
     */
    List<PermissionNodeVO> getPermissionTree();

    /**
     * 查询所有角色及其拥有的权限
     * @return
     */
    List<RoleVO> getAllRolesWithPermissions();

    /**
     * 查询指定角色拥有的权限ID列表
     * @param roleId
     * @return
     */
    List<Long> getPermissionIdsByRoleId(Integer roleId);

    /**
     * 更新角色权限（全量覆盖）
     * @param roleId
     * @param dto
     */
    void updateRolePermissions(Integer roleId, RolePermissionUpdateDTO dto);

    /**
     * 根据角色ID查询权限代码集合（用于拦截器校验）
     * @param roleId
     * @return
     */
    Set<String> getPermissionCodesByRoleId(Integer roleId);

}