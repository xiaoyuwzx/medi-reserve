package com.medireserve.doctor.service.impl;

import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.DoctorRegisterDTO;
import com.medireserve.common.entity.Department;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.DoctorAudit;
import com.medireserve.common.entity.Title;
import com.medireserve.common.exception.*;
import com.medireserve.common.service.LoginAttemptService;
import com.medireserve.common.utils.PasswordUtil;
import com.medireserve.common.mapper.DepartmentMapper;
import com.medireserve.common.mapper.DoctorAuthMapper;
import com.medireserve.common.mapper.DoctorAuditMapper;
import com.medireserve.common.mapper.TitleMapper;
import com.medireserve.doctor.service.DoctorAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 医生端认证
 */
@Slf4j
@Service
public class DoctorAuthServiceImpl implements DoctorAuthService {

    @Autowired
    private DoctorAuthMapper doctorAuthMapper;

    @Autowired
    private DoctorAuditMapper doctorAuditMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private TitleMapper titleMapper;

    @Autowired
    private LoginAttemptService loginAttemptService;

    /**
     * 医生注册
     * @param registerDTO
     * @return
     */
    @Override
    @Transactional // 使用事务注解，保证两张表数据一致
    public Doctor register(DoctorRegisterDTO registerDTO) {

        //判断手机号是否被注册
        Doctor existing = doctorAuthMapper.findByPhone(registerDTO.getPhone());
        if(existing != null){
            log.warn("医生注册失败，手机号已存在：{}", registerDTO.getPhone());
            throw new PhoneAlreadyExistsException();
        }

        //校验科室是否存在
        Department department = departmentMapper.findById(registerDTO.getDepartmentId());
        if (department == null) {
            log.warn("医生注册失败，所选科室不存在：{}", registerDTO.getPhone());
            throw new DepartmentNotFoundException();
        }

        //校验职称是否存在
        Title title = titleMapper.findById(registerDTO.getTitleId());
        if (title == null) {
            log.warn("医生注册失败，所选职称不存在：{}", registerDTO.getPhone());
            throw new TitleNotFoundException();
        }

        //注册
        //创建医生数据
        Doctor doctor = new Doctor();
        BeanUtils.copyProperties(registerDTO, doctor);
        //使用BCrypt加密密码
        doctor.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        //设置账号状态
        doctor.setStatus(StatusConstant.ACCOUNT_NORMAL);
        //插入医生账号数据
        doctorAuthMapper.insert(doctor);
        //创建医生审核数据
        DoctorAudit doctorAudit = new DoctorAudit();
        doctorAudit.setDoctorId(doctor.getId());
        doctorAudit.setSpecialty(registerDTO.getSpecialty());
        doctorAudit.setIntroduction(registerDTO.getIntroduction());
        doctorAudit.setAuditStatus(StatusConstant.AUDIT_PENDING);
        //插入医生审核数据
        doctorAuditMapper.insert(doctorAudit);

        log.info("医生注册成功，ID：{}，手机号：{}，等待审核", doctor.getId(), doctor.getPhone());

        return doctor;

    }

    /**
     * 医生登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public Doctor login(String username, String password) {

        // 检查是否已被锁定（登录前检查）
        loginAttemptService.checkAttempts(username);

        Doctor doctor = doctorAuthMapper.findByPhone(username);

        //查询手机号判断是否被注册
        if (doctor == null){
            log.warn("医生登录失败，手机号未注册：{}", username);
            loginAttemptService.loginFailed(username);
            throw new AccountNotFoundException();
        }

        //校验密码
        if(!PasswordUtil.matches(password, doctor.getPassword())){
            log.warn("医生登录失败，密码错误，手机号：{}", username);
            loginAttemptService.loginFailed(username);
            throw  new PasswordErrorException();
        }

        //校验状态
        if(StatusConstant.ACCOUNT_DISABLED.equals(doctor.getStatus())){
            log.warn("医生登录失败，账号已被禁用，手机号：{}", username);
            throw new AccountDisabledException();
        }

        //校验审核状态
        DoctorAudit doctorAudit = doctorAuditMapper.findByDoctorId(doctor.getId());
        if (doctorAudit == null) {
            // 理论上不会发生（注册时已创建），但做防御性处理
            log.error("医生审核数据不存在，医生ID：{}", doctor.getId());
            throw new DoctorAuditNotFoundException();
        }
        // 判断审核状态
        if (StatusConstant.AUDIT_PENDING.equals(doctorAudit.getAuditStatus())) {
            log.warn("医生登录失败，账号审核中，手机号：{}", username);
            throw new AuditPendingException();
        }
        if (StatusConstant.AUDIT_REJECTED.equals(doctorAudit.getAuditStatus())) {
            log.warn("医生登录失败，账号审核未通过，手机号：{}", username);
            throw new AuditRejectedException();
        }

        log.info("医生登录成功，手机号：{}", username);

        // 登录成功，清除失败计数
        loginAttemptService.loginSucceeded(username);

        return doctor;

    }

}
