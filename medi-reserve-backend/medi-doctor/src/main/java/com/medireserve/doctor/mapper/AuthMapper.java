package com.medireserve.doctor.mapper;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.entity.Doctor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * 医生端认证
 */
@Mapper
public interface AuthMapper {

    /**
     * 根据手机号查找医生数据
     * @param phone
     * @return
     */
    @Select("select * from doctor where phone = #{phone}")
    Doctor findByPhone(String phone);

    /**
     * 插入医生数据
     * @param doctor
     * @return
     */
    @Insert("insert into doctor (name, phone, password, id_card, gender, department, title, status) " +
            "values (#{name}, #{phone},#{password}, #{idCard}, #{gender}, #{department}, #{title}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Doctor doctor);

}
