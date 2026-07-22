package com.medireserve.admin.mapper;

import com.medireserve.common.entity.Permission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限 Mapper 接口
 */
@Mapper
public interface PermissionMapper {

    /**
     * 查询所有权限（树形结构，按 sort_order 排序）
     * @return
     */
    List<Permission> findAll();

    /**
     * 根据角色ID查询该角色拥有的权限列表
     * @param roleId
     * @return
     */
    List<Permission> findByRoleId(@Param("roleId") Integer roleId);

    /**
     * 根据权限代码查询权限
     * @param code
     * @return
     */
    @Select("SELECT * FROM permission WHERE code = #{code}")
    Permission findByCode(@Param("code") String code);

    /**
     * 插入权限（用于初始化，实际业务可能不需要）
     * @param permission
     * @return
     */
    int insert(Permission permission);

    /**
     * 更新权限
     * @param permission
     * @return
     */
    int update(Permission permission);

    /**
     * 删除权限
     * @param id
     * @return
     */
    @Delete("DELETE FROM permission WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

}