package com.medireserve.admin.service;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.AdminDoctorQueryDTO;
import com.medireserve.common.dto.AdminDoctorStatusDTO;
import com.medireserve.common.dto.AdminDoctorVO;

/**
 * 管理员端 - 医生账号管理 Service
 */
public interface AdminDoctorManageService {

    /**
     * 分页查询医生账号列表
     * @param query 查询条件
     * @return 分页结果
     */
    PageInfo<AdminDoctorVO> listDoctors(AdminDoctorQueryDTO query);

    /**
     * 查询医生详情
     * @param doctorId 医生ID
     * @return 医生详情
     */
    AdminDoctorVO getDoctorDetail(Long doctorId);

    /**
     * 修改医生账号状态（启用/禁用）
     * @param doctorId 医生ID
     * @param dto 状态DTO
     */
    void updateDoctorStatus(Long doctorId, AdminDoctorStatusDTO dto);
}