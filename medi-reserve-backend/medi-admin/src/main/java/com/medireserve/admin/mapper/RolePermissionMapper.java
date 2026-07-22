package com.medireserve.admin.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联 Mapper
 */
@Mapper
public interface RolePermissionMapper {

    /**
     * 批量插入角色权限关联
     */
    int batchInsert(@Param("roleId") Integer roleId,
                    @Param("permissionIds") List<Long> permissionIds);

    /**
     * 删除角色的所有权限关联
     */
    @Delete("DELETE FROM role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Integer roleId);

    /**
     * 查询角色拥有的权限ID列表
     */
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Integer roleId);

}