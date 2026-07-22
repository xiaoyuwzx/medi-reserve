package com.medireserve.doctor.service;

import com.medireserve.common.dto.DoctorAuditInfoVO;
import com.medireserve.common.dto.DoctorRegisterDTO;
import com.medireserve.common.dto.DoctorUpdateDTO;
import com.medireserve.common.dto.PasswordUpdateDTO;
import com.medireserve.common.entity.Doctor;

import java.util.Map;

/**
 * 医生端认证
 */
public interface DoctorAuthService {

    /**
     * 医生注册
     * @param registerDTO
     * @return
     */
    Doctor register(DoctorRegisterDTO registerDTO);

    /**
     * 医生登录
     * @param username
     * @param password
     * @return
     */
    Doctor login(String username, String password);

    /**
     * 修改密码
     */
    void updatePassword(Long doctorId, PasswordUpdateDTO dto);

    /**
     * 更新医生个人信息
     * 普通信息（姓名、手机号、性别、身份证号）立即生效
     * 证件信息（执业证书、资格证）提交审核，不立即生效
     * @param doctorId 当前医生ID
     * @param dto 更新数据
     * @return 包含新Token（如果手机号变更）和更新后的用户信息
     */
    Map<String, Object> updateProfile(Long doctorId, DoctorUpdateDTO dto);

    /**
     * 查询医生证件审核状态
     */
    DoctorAuditInfoVO getAuditStatus(Long doctorId);

}
