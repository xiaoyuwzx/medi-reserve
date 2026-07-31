package com.medireserve.admin.service;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.AdminPatientQueryDTO;
import com.medireserve.common.dto.AdminPatientStatusDTO;
import com.medireserve.common.dto.AdminPatientVO;

/**
 * 管理员端 - 患者账号管理 Service
 */
public interface AdminPatientManageService {

    /**
     * 分页查询患者账号列表
     * @param query 查询条件
     * @return 分页结果
     */
    PageInfo<AdminPatientVO> listPatients(AdminPatientQueryDTO query);

    /**
     * 查询患者详情
     * @param patientId 患者ID
     * @return 患者详情
     */
    AdminPatientVO getPatientDetail(Long patientId);

    /**
     * 修改患者账号状态（启用/禁用）
     * @param patientId 患者ID
     * @param dto 状态DTO
     */
    void updatePatientStatus(Long patientId, AdminPatientStatusDTO dto);
}