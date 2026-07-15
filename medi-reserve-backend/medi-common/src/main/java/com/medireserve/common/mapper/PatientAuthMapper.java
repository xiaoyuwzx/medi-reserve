package com.medireserve.common.mapper;

import com.medireserve.common.entity.Patient;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * 患者端认证接口
 */
@Mapper
public interface PatientAuthMapper {

    /**
     * 根据手机号查询患者账号
     * @param phone
     * @return 返回患者对象
     */
    @Select("select * from patient where phone = #{phone}")
    Patient findByPhone(String phone);

    /**
     * 插入患者数据（患者注册）
     * @param patient
     * @return 返回影响行数
     */
    @Insert("insert into patient (name, phone, password, id_card, gender, status)" +
            " values (#{name}, #{phone}, #{password}, #{idCard}, #{gender}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Patient patient);

    /**
     * 根据患者ID查询患者信息
     * @param patientId
     * @return
     */
    @Select("select * from patient where id = #{patientId}")
    Patient findById(Long patientId);
}
