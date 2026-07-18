package com.medireserve.admin.service;

import com.medireserve.common.dto.AdminRegisterDTO;
import com.medireserve.common.entity.Admin;
import com.github.pagehelper.PageInfo;

/**
 * 管理端认证
 */
public interface AdminAuthService {

    /**
     * 管理员注册
     * @param registerDTO
     * @return
     */
    Admin register(AdminRegisterDTO registerDTO);

    /**
     * 管理员登录
     * @param username
     * @param password
     * @return
     */
    Admin login(String username, String password);

    /**
     * 获取管理员列表（分页）
     */
    PageInfo<Admin> getAdminList(int page, int size);

    /**
     * 修改管理员状态（禁用/启用）
     */
    void updateAdminStatus(Long adminId, Integer status, Long currentAdminId);
}
