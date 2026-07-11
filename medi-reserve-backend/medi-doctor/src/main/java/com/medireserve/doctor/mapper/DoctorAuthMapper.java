package com.medireserve.doctor.mapper;

import com.medireserve.common.entity.Doctor;
import org.apache.ibatis.annotations.*;

/**
 * 医生端认证
 */
@Mapper
public interface DoctorAuthMapper {

    /**
     * 根据手机号查找医生数据
     * @param phone
     * @return
     */
    @Select("SELECT d.*, dept.name AS departmentName, t.name AS titleName " +
            "FROM doctor d " +
            "LEFT JOIN department dept ON d.department_id = dept.id " +
            "LEFT JOIN title t ON d.title_id = t.id " +
            "WHERE d.phone = #{phone}")
    Doctor findByPhone(String phone);

    /**
     * 插入医生数据
     * @param doctor
     * @return
     */
    @Insert("INSERT INTO doctor (name, phone, password, id_card, gender, birth_date, " +
            "department_id, title_id, status) " +
            "VALUES (#{name}, #{phone}, #{password}, #{idCard}, #{gender}, #{birthDate}, " +
            "#{departmentId}, #{titleId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Doctor doctor);

    /**
     * 根据医生ID查询医生数据
     * @param id
     * @return
     */
    @Select("SELECT d.*, dept.name AS departmentName, t.name AS titleName " +
            "FROM doctor d " +
            "LEFT JOIN department dept ON d.department_id = dept.id " +
            "LEFT JOIN title t ON d.title_id = t.id " +
            "WHERE d.id = #{id}")
    Doctor findById(@Param("id") Long id);

}
