package com.medireserve.common.mapper;

import com.medireserve.common.entity.Doctor;
import org.apache.ibatis.annotations.*;

import java.util.List;

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

    /**
     * 查询所有已审核通过且账号状态正常的医生ID
     * 用于初始化布隆过滤器
     */
    @Select("SELECT d.id FROM doctor d " +
            "INNER JOIN doctor_audit da ON d.id = da.doctor_id " +
            "WHERE d.status = 1 AND da.audit_status = 1")
    List<Long> findAllApprovedIds();

    /**
     * 修改密码
     */
    @Update("update doctor set password = #{password} where id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新医生基本信息（不包含密码、科室、职称）
     * 普通信息立即生效
     */
    @Update("UPDATE doctor SET name = #{name}, phone = #{phone}, id_card = #{idCard}, gender = #{gender} " +
            "WHERE id = #{id}")
    int updateById(Doctor doctor);

    /**
     * 统计除指定ID外该手机号的数量（用于修改时校验唯一性）
     */
    @Select("SELECT COUNT(*) FROM doctor WHERE phone = #{phone} AND id != #{id}")
    int countByPhoneAndNotId(@Param("phone") String phone, @Param("id") Long id);
}
