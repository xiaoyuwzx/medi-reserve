package com.medireserve.patient.service.impl;

import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.PatientRegisterDTO;
import com.medireserve.common.entity.Patient;
import com.medireserve.common.exception.AccountDisabledException;
import com.medireserve.common.exception.AccountNotFoundException;
import com.medireserve.common.exception.PasswordErrorException;
import com.medireserve.common.exception.PhoneAlreadyExistsException;
import com.medireserve.common.utils.PasswordUtil;
import com.medireserve.patient.mapper.AuthMapper;
import com.medireserve.patient.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 患者端认证接口
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthMapper authMapper;

    /**
     * 患者注册
     * @param registerDTO
     * @return
     */
    @Override
    public Patient register(PatientRegisterDTO registerDTO) {

        //判断手机号是否被注册
        Patient existing = authMapper.findByPhone(registerDTO.getPhone());
        if(existing != null) {
            log.warn("患者注册失败，手机号已存在：{}", registerDTO.getPhone());
            throw new PhoneAlreadyExistsException();
        }

        //注册
        Patient patient = new Patient();
        BeanUtils.copyProperties(registerDTO, patient);
        //使用BCrypt 加密密码
        patient.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        //设置账号状态
        patient.setStatus(StatusConstant.ACCOUNT_NORMAL);
        //保存到数据库中
        authMapper.insert(patient);

        log.info("患者注册成功，ID：{}，手机号：{}", patient.getId(), patient.getPhone());

        return patient;

    }

    @Override
    public Patient login(String phone, String password) {

        //判断用户是否存在
        Patient patient = authMapper.findByPhone(phone);
        if(patient == null){
            log.warn("患者登录失败，手机号未注册：{}", phone);
            throw new AccountNotFoundException();
        }

        //密码校验
        if(!PasswordUtil.matches(password, patient.getPassword())){
            log.warn("患者登录失败，密码错误：{}", password);
            throw new PasswordErrorException();
        }

        //判断账号状态
        if(StatusConstant.ACCOUNT_DISABLED.equals(patient.getStatus())){
            log.warn("患者登录失败，账号已被禁用：{}", phone);
            throw new AccountDisabledException();
        }

        log.info("患者登录成功：{}", phone);

        return patient;

    }

}
