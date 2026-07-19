package com.medireserve.admin.service.impl;

import com.medireserve.admin.mapper.AdminAuthMapper;
import com.medireserve.admin.service.AdminAuthService;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.AdminRegisterDTO;
import com.medireserve.common.dto.PasswordUpdateDTO;
import com.medireserve.common.entity.Admin;
import com.medireserve.common.exception.*;
import com.medireserve.common.service.LoginAttemptService;
import com.medireserve.common.utils.PasswordUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端认证
 */
@Slf4j
@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    @Autowired
    private AdminAuthMapper adminAuthMapper;

    @Autowired
    private LoginAttemptService loginAttemptService;

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

        // 检查是否已被锁定（登录前检查）
        loginAttemptService.checkAttempts(username);

        Admin admin = adminAuthMapper.findByUsername(username);

        //判断账号是否注册
        if(admin == null){
            log.warn("管理员登录失败，用户名不存在：{}", username);
            loginAttemptService.loginFailed(username);
            throw new AccountNotFoundException();
        }

        //判断密码是否正确
        if(!PasswordUtil.matches(password, admin.getPassword())){
            log.warn("管理员登录失败，密码错误，用户名：{}", username);
            loginAttemptService.loginFailed(username);
            throw new PasswordErrorException();
        }

        //判断账号状态
        if(StatusConstant.ACCOUNT_DISABLED.equals(admin.getStatus())){
            log.warn("管理员登录失败，账号已被禁用，用户名：{}", username);
            throw new AccountDisabledException();
        }

        log.info("管理员登录成功，用户名：{}", username);

        // 登录成功，清除失败计数
        loginAttemptService.loginSucceeded(username);

        return admin;

    }

    /**
     * 获取管理员列表（分页）
     */
    @Override
    public PageInfo<Admin> getAdminList(int page, int size) {
        log.info("获取管理员列表，页码：{}，每页：{}", page, size);

        PageHelper.startPage(page, size);
        List<Admin> list = adminAuthMapper.findAll();
        int total = adminAuthMapper.countAll();

        PageInfo<Admin> pageInfo = new PageInfo<>(list);
        pageInfo.setTotal(total);
        return pageInfo;
    }

    /**
     * 修改管理员状态（禁用/启用）
     */
    @Override
    public void updateAdminStatus(Long adminId, Integer status, Long currentAdminId) {
        log.info("修改管理员状态，管理员ID：{}，目标状态：{}，操作人：{}", adminId, status, currentAdminId);

        // 不允许禁用自己
        if (adminId.equals(currentAdminId) && StatusConstant.ACCOUNT_DISABLED.equals(status)) {
            log.warn("尝试禁用自己，管理员ID：{}", adminId);
            throw new BusinessException("不能禁用当前登录的管理员账号");
        }

        int rows = adminAuthMapper.updateStatus(adminId, status);
        if (rows == 0) {
            log.warn("修改管理员状态失败，管理员不存在，ID：{}", adminId);
            throw new BusinessException("管理员不存在");
        }

        log.info("管理员状态修改成功，ID：{}，状态：{}", adminId, status);
    }

    @Override
    public void updatePassword(Long adminId, PasswordUpdateDTO dto) {

        Admin admin = adminAuthMapper.findById(adminId);
        if (admin == null) {
            throw new AccountNotFoundException();
        }

        if (!PasswordUtil.matches(dto.getOldPassword(), admin.getPassword())) {
            throw new PasswordErrorException();
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        adminAuthMapper.updatePassword(adminId, PasswordUtil.encode(dto.getNewPassword()));
        log.info("管理员密码修改成功，ID：{}", adminId);
    }
}
