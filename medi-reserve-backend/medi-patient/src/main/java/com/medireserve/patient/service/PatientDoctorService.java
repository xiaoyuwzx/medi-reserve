package com.medireserve.patient.service;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.DepartmentVO;
import com.medireserve.common.dto.DoctorListQueryDTO;
import com.medireserve.common.dto.DoctorListVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 患者端 - 医生/排班查询服务
 * 提供科室列表、医生列表、排班日历等查询功能
 */
public interface PatientDoctorService {

    /**
     * 获取所有科室列表
     * @return
     */
    List<DepartmentVO> getAllDepartments();

    /**
     * 分页查询医生列表
     * @param doctorListQueryDTO
     * @return
     */
    PageInfo<DoctorListVO> getDoctorList(DoctorListQueryDTO doctorListQueryDTO);
}
