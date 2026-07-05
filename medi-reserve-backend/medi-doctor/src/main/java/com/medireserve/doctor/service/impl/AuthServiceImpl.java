package com.medireserve.doctor.service.impl;

import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.DoctorRegisterDTO;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.DoctorAudit;
import com.medireserve.common.exception.AccountDisabledException;
import com.medireserve.common.exception.AccountNotFoundException;
import com.medireserve.common.exception.PasswordErrorException;
import com.medireserve.common.exception.PhoneAlreadyExistsException;
import com.medireserve.common.utils.PasswordUtil;
import com.medireserve.doctor.mapper.AuthMapper;
import com.medireserve.doctor.mapper.DoctorAuditMapper;
import com.medireserve.doctor.service.AuthService;
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
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthMapper authMapper;

    @Autowired
    private DoctorAuditMapper doctorAuditMapper;

    /**
     * 医生注册
     * @param registerDTO
     * @return
     */
    @Override
    @Transactional // 使用事务注解，保证两张表数据一致
    public Doctor register(DoctorRegisterDTO registerDTO) {

        //判断手机号是否被注册
        Doctor existing = authMapper.findByPhone(registerDTO.getPhone());
        if(existing != null){
            log.warn("医生注册失败，手机号已存在：{}", registerDTO.getPhone());
            throw new PhoneAlreadyExistsException();
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
        authMapper.insert(doctor);
        //创建医生审核数据
        DoctorAudit doctorAudit = new DoctorAudit();
        doctorAudit.setDoctorId(doctor.getId());
        doctorAudit.setSpecialty(registerDTO.getSpecialty());
        doctorAudit.setIntroduction(registerDTO.getIntroduction());
        doctorAudit.setAuditStatus(StatusConstant.AUDIT_PENDING);
        //插入医生审核数据
        doctorAuditMapper.insert(doctorAudit);

        log.info("医生注册成功，ID：{}，手机号：{}", doctor.getId(), doctor.getPhone());

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

        //查询手机号判断是否被注册
        Doctor doctor = authMapper.findByPhone(username);
        if (doctor == null){
            log.warn("医生登录失败，手机号未注册：{}", username);
            throw new AccountNotFoundException();
        }

        //校验密码
        if(!PasswordUtil.matches(password, doctor.getPassword())){
            log.warn("医生登录失败，密码错误，手机号：{}", username);
            throw  new PasswordErrorException();
        }

        //校验状态
        if(StatusConstant.ACCOUNT_DISABLED.equals(doctor.getStatus())){
            log.warn("医生登录失败，账号已被禁用，手机号：{}", username);
            throw new AccountDisabledException();
        }

        log.info("医生登录成功，手机号：{}", username);

        return doctor;

    }

}
