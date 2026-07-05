package com.medireserve.patient.service;

import com.medireserve.common.dto.PatientRegisterDTO;
import com.medireserve.common.entity.Patient;

/**
 * 患者端认证接口
 */
public interface AuthService {

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
}
