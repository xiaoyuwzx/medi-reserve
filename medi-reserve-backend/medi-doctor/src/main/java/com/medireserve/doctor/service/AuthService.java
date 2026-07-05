package com.medireserve.doctor.service;

import com.medireserve.common.dto.DoctorRegisterDTO;
import com.medireserve.common.entity.Doctor;

/**
 * 医生端认证
 */
public interface AuthService {

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
}
