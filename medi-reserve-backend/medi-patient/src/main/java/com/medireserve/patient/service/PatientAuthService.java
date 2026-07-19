package com.medireserve.patient.service;

import com.medireserve.common.dto.PasswordUpdateDTO;
import com.medireserve.common.dto.PatientRegisterDTO;
import com.medireserve.common.dto.PatientUpdateDTO;
import com.medireserve.common.entity.Patient;

import java.util.Map;

/**
 * 患者端认证接口
 */
public interface PatientAuthService {

    /**
     * 患者注册
     * @param registerDTO
     * @return
     */
    Patient register(PatientRegisterDTO registerDTO);

    /**
     * 患者登录
     * @param phone
     * @param password
     * @return
     */
    Patient login(String phone, String password);

    /**
     * 修改个人信息
     * @param patientId 当前患者ID
     * @param dto 修改参数
     * @return 包含新Token（如手机号变更）和患者信息的Map
     */
    Map<String, Object> updateProfile(Long patientId, PatientUpdateDTO dto);

    /**
     * 修改密码
     */
    void updatePassword(Long patientId, PasswordUpdateDTO dto);
}
