package com.medireserve.patient.service;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.dto.LoginDTO;
import com.medireserve.common.dto.PatientRegisterDTO;
import com.medireserve.common.entity.Patient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 患者端认证接口
 */
public interface AuthService {

    /**
     * 患者注册
     * @param registerDTO
     * @return
     */
    Patient register(@Valid PatientRegisterDTO registerDTO);

    /**
     * 患者登录
     * @param phone
     * @param password
     * @return
     */
    Patient login(String phone, String password);
}
