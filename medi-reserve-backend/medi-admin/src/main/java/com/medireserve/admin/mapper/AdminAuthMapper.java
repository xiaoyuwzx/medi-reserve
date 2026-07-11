package com.medireserve.admin.mapper;

import com.medireserve.common.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

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

}
