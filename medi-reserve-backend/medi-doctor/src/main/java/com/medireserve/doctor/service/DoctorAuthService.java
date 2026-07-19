package com.medireserve.doctor.service;

import com.medireserve.common.dto.DoctorRegisterDTO;
import com.medireserve.common.dto.PasswordUpdateDTO;
import com.medireserve.common.entity.Doctor;

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
}
