package com.medireserve.admin.mapper;

import com.medireserve.common.entity.Admin;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 管理端认证
 */
@Mapper
public interface AdminAuthMapper {

    @Select("select * from admin where username = #{username}")
    Admin findByUsername(String username);

    @Select("select * from admin where phone = #{phone}")
    Admin findByPhone(String phone);

    @Insert("insert into admin (username, password, name, phone, email, role, status) " +
            "values (#{username}, #{password}, #{name}, #{phone}, #{email}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Admin admin);

    @Select("select * from admin order by case when role = 1 then 0 else 1 end, created_at desc")
    List<Admin> findAll();

    @Select("select count(*) from admin")
    int countAll();

    @Select("select * from admin where id = #{id}")
    Admin findById(@Param("id") Long id);

    @Update("update admin set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

}
