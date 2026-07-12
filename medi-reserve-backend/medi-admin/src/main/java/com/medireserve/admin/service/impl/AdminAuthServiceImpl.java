package com.medireserve.admin.service.impl;

import com.medireserve.admin.mapper.AdminAuthMapper;
import com.medireserve.admin.service.AdminAuthService;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.AdminRegisterDTO;
import com.medireserve.common.entity.Admin;
import com.medireserve.common.exception.*;
import com.medireserve.common.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 管理端认证
 */
@Slf4j
@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    @Autowired
    private AdminAuthMapper adminAuthMapper;

    /**
     * 管理员注册
     * @param registerDTO
     * @return
     */
    @Override
    public Admin register(AdminRegisterDTO registerDTO) {

        //校验权限（只有超级管理员才可以操作）
        //移除 currentRole 参数和角色校验，由拦截器统一处理

        //检查用户名是否被注册
        Admin existingByUsername = adminAuthMapper.findByUsername(registerDTO.getUsername());
        if(existingByUsername != null){
            log.warn("管理员注册失败，用户名已被占用：{}", registerDTO.getUsername());
            throw new UsernameAlreadyExistsException();
        }

        //检查手机号是否被注册
        if(registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()){
            Admin existingByPhone = adminAuthMapper.findByPhone(registerDTO.getPhone());
            if(existingByPhone != null){
                log.warn("管理员注册失败，手机号已被占用：{}", registerDTO.getPhone());
                throw new PhoneAlreadyExistsException();
            }
        }

        //创建管理员
        Admin admin = new Admin();
        BeanUtils.copyProperties(registerDTO, admin);
        //使用BCrypt加密密码
        admin.setPassword(PasswordUtil.encode(registerDTO.getPassword()));

        //设置权限（默认为普通管理员）
        // 强制设为普通管理员（不允许通过注册接口创建超级管理员）
        admin.setRole(RoleConstant.ADMIN_NORMAL);
        // 如果前端传入了其他角色值，记录日志供审计
        if(registerDTO.getRole() != null && !RoleConstant.ADMIN_NORMAL.equals(registerDTO.getRole())){
            log.warn("尝试创建非普通管理员角色，已强制覆盖：{} -> 普通管理员", registerDTO.getRole());
        }

        //设置账号状态
        admin.setStatus(StatusConstant.ACCOUNT_NORMAL);

        //保存进数据库中
        adminAuthMapper.insert(admin);

        log.info("管理员注册成功，用户名：{}，ID：{}，角色：{}", admin.getUsername(), admin.getId(), admin.getRole());

        return admin;

    }

    /**
     * 管理员登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public Admin login(String username, String password) {

        Admin admin = adminAuthMapper.findByUsername(username);

        //判断账号是否注册
        if(admin == null){
            log.warn("管理员登录失败，用户名不存在：{}", username);
            throw new AccountNotFoundException();
        }

        //判断密码是否正确
        if(!PasswordUtil.matches(password, admin.getPassword())){
            log.warn("管理员登录失败，密码错误，用户名：{}", username);
            throw new PasswordErrorException();
        }

        //判断账号状态
        if(StatusConstant.ACCOUNT_DISABLED.equals(admin.getStatus())){
            log.warn("管理员登录失败，账号已被禁用，用户名：{}", username);
            throw new AccountDisabledException();
        }

        log.info("管理员登录成功，用户名：{}", username);

        return admin;

    }
}
