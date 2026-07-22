package com.medireserve.admin.service.impl;

import com.medireserve.admin.mapper.PermissionMapper;
import com.medireserve.admin.mapper.RoleMapper;
import com.medireserve.admin.mapper.RolePermissionMapper;
import com.medireserve.common.dto.PermissionNodeVO;
import com.medireserve.common.dto.RolePermissionUpdateDTO;
import com.medireserve.common.dto.RoleVO;
import com.medireserve.common.entity.Permission;
import com.medireserve.common.entity.Role;
import com.medireserve.common.exception.BusinessException;
import com.medireserve.common.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    /**
     * 构建权限树
     */
    @Override
    public List<PermissionNodeVO> getPermissionTree() {

        //查询所有权限
        List<Permission> all = permissionMapper.findAll();

        // 按 parentId 分组
        Map<Long, List<Permission>> groupMap = all.stream()
                .collect(Collectors.groupingBy(Permission::getParentId));

        // 构建树（根节点 parentId = 0）
        return buildTree(0L, groupMap);

    }

    private List<PermissionNodeVO> buildTree(Long parentId, Map<Long, List<Permission>> groupMap) {
        List<Permission> children = groupMap.getOrDefault(parentId, Collections.emptyList());
        return children.stream()
                .sorted(Comparator.comparingInt(Permission::getSortOrder))
                .map(p -> {
                    PermissionNodeVO node = new PermissionNodeVO();
                    BeanUtils.copyProperties(p, node);
                    // 递归构建子节点
                    node.setChildren(buildTree(p.getId(), groupMap));
                    return node;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询所有角色及其拥有的权限
     * @return
     */
    @Override
    public List<RoleVO> getAllRolesWithPermissions() {

        List<Role> roles = roleMapper.findAll();

        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            // 查询该角色拥有的权限ID
            List<Long> permissionIds = rolePermissionMapper.findPermissionIdsByRoleId(role.getId());
            vo.setPermissionIds(permissionIds);
            return vo;
        }).collect(Collectors.toList());

    }

    /**
     * 查询指定角色拥有的权限ID列表
     * @param roleId
     * @return
     */
    @Override
    public List<Long> getPermissionIdsByRoleId(Integer roleId) {

        return rolePermissionMapper.findPermissionIdsByRoleId(roleId);

    }

    /**
     * 更新角色权限（全量覆盖）
     * @param roleId
     * @param dto
     */
    @Transactional
    @Override
    public void updateRolePermissions(Integer roleId, RolePermissionUpdateDTO dto) {

        // 校验角色是否存在
        Role role = roleMapper.findById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 校验权限是否存在（可选）
        List<Long> permIds = dto.getPermissionIds();
        if (permIds != null && !permIds.isEmpty()) {
            // 可进一步校验所有 permissionId 是否有效，此处简单处理
        }

        // 删除原有权限关联
        rolePermissionMapper.deleteByRoleId(roleId);

        // 批量插入新关联
        if (permIds != null && !permIds.isEmpty()) {
            rolePermissionMapper.batchInsert(roleId, permIds);
        }

        log.info("角色权限更新成功，角色ID：{}，权限数：{}", roleId, permIds == null ? 0 : permIds.size());
    }

    /**
     * 根据角色ID查询权限代码集合（用于拦截器校验）
     * @param roleId
     * @return
     */
    @Override
    public Set<String> getPermissionCodesByRoleId(Integer roleId) {

        List<Permission> permissions = permissionMapper.findByRoleId(roleId);

        return permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

    }

}
