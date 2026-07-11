package com.medireserve.admin.service;

import com.medireserve.common.dto.AdminRegisterDTO;
import com.medireserve.common.entity.Admin;

/**
 * 管理端认证
 */
public interface AdminAuthService {

    /**
     * 管理员注册
     * @param registerDTO
     * @param currentRole
     * @return
     */
    Admin register(AdminRegisterDTO registerDTO, String currentRole);

    /**
     * 管理员登录
     * @param username
     * @param password
     * @return
     */
    Admin login(String username, String password);
}
