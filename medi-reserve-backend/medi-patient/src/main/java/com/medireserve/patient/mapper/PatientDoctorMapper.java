package com.medireserve.patient.mapper;

import com.medireserve.common.dto.DepartmentVO;
import com.medireserve.common.dto.DoctorListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 患者端 - 医生查询 Mapper
 * 注意：此 Mapper 放在患者端子包下，不在 common 中
 * 提供科室列表和医生列表查询（只读操作）
 */
@Mapper
public interface PatientDoctorMapper {

    /**
     * 查询所有科室列表
     * 只统计已审核且账号状态正常的医生
     * @return
     */
    @Select("SELECT dept.name AS department, COUNT(*) AS doctorCount " +
            "FROM doctor d INNER JOIN doctor_audit da " +
            "ON d.id = da.doctor_id " +
            "LEFT JOIN department dept ON d.department_id = dept.id " +
            "WHERE d.status = 1 AND da.audit_status = 1 " +
            "GROUP BY dept.id, dept.name " +
            "ORDER BY dept.name ASC")
    List<DepartmentVO> findAllDepartments();

    /**
     * 分页查询医生列表
     * @param department
     * @param keyword
     * @return
     */
    List<DoctorListVO> findDoctorList(@Param("department") String department,
                                      @Param("keyword") String keyword);
}
