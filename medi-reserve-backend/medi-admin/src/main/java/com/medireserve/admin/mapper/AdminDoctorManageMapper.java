package com.medireserve.admin.mapper;

import com.medireserve.common.dto.AdminDoctorQueryDTO;
import com.medireserve.common.dto.AdminDoctorVO;
import com.medireserve.common.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 管理员端 - 医生账号管理 Mapper
 */
@Mapper
public interface AdminDoctorManageMapper {

    /**
     * 分页查询医生账号列表（支持关键词、科室、状态、审核状态筛选）
     * @param query 查询条件
     * @return 医生列表
     */
    List<AdminDoctorVO> findDoctorList(@Param("q") AdminDoctorQueryDTO query);

    /**
     * 统计符合条件的医生总数
     */
    long countDoctorList(@Param("q") AdminDoctorQueryDTO query);

    /**
     * 根据ID查询医生详情（含审核资料）
     */
    @Select("SELECT d.*, dept.name AS departmentName, t.name AS titleName, " +
            "da.specialty, da.introduction, da.avatar, da.audit_status AS auditStatus " +
            "FROM doctor d " +
            "LEFT JOIN department dept ON d.department_id = dept.id " +
            "LEFT JOIN title t ON d.title_id = t.id " +
            "LEFT JOIN doctor_audit da ON d.id = da.doctor_id " +
            "WHERE d.id = #{doctorId}")
    AdminDoctorVO findDoctorDetail(@Param("doctorId") Long doctorId);

    /**
     * 更新医生账号状态
     */
    @Update("UPDATE doctor SET status = #{status} WHERE id = #{id}")
    int updateDoctorStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据ID查询医生（仅用于状态校验）
     */
    @Select("SELECT * FROM doctor WHERE id = #{id}")
    Doctor findById(@Param("id") Long id);
}