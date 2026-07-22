package com.medireserve.admin.mapper;

import com.medireserve.common.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Select("SELECT * FROM role ORDER BY id")
    List<Role> findAll();

    @Select("SELECT * FROM role WHERE id = #{id}")
    Role findById(@Param("id") Integer id);

}