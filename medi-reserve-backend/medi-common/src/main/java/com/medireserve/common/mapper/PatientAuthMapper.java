package com.medireserve.common.mapper;

import com.medireserve.common.entity.Patient;
import org.apache.ibatis.annotations.*;

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

    /**
     * 统计除指定ID外的手机号数量（用于修改时校验唯一性）
     */
    @Select("select count(*) from patient where phone = #{phone} and id != #{id}")
    int countByPhoneAndNotId(@Param("phone") String phone, @Param("id") Long id);

    /**
     * 根据ID更新患者信息
     */
    @Update("update patient set name = #{name}, phone = #{phone}, id_card = #{idCard}, gender = #{gender} where id = #{id}")
    int updateById(Patient patient);

    /**
     * 修改密码
     */
    @Update("update patient set password = #{password} where id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
