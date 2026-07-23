package com.medireserve.patient.service.impl;

import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.PasswordUpdateDTO;
import com.medireserve.common.dto.PatientRegisterDTO;
import com.medireserve.common.dto.PatientUpdateDTO;
import com.medireserve.common.entity.Patient;
import com.medireserve.common.exception.*;
import com.medireserve.common.service.LoginAttemptService;
import com.medireserve.common.utils.JwtUtil;
import com.medireserve.common.utils.PasswordUtil;
import com.medireserve.common.mapper.PatientAuthMapper;
import com.medireserve.patient.service.PatientAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 患者端认证接口
 */
@Slf4j
@Service
public class PatientAuthServiceImpl implements PatientAuthService {

    @Autowired
    private PatientAuthMapper patientAuthMapper;

    @Autowired
    private LoginAttemptService loginAttemptService;

    /**
     * 患者注册
     * @param registerDTO
     * @return
     */
    @Override
    public Patient register(PatientRegisterDTO registerDTO) {

        //判断手机号是否被注册
        Patient existing = patientAuthMapper.findByPhone(registerDTO.getPhone());
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
        patientAuthMapper.insert(patient);

        log.info("患者注册成功，ID：{}，手机号：{}", patient.getId(), patient.getPhone());

        return patient;

    }

    @Override
    public Patient login(String phone, String password) {

        // 检查是否已被锁定（登录前检查）
        loginAttemptService.checkAttempts(phone);

        //判断用户是否存在
        Patient patient = patientAuthMapper.findByPhone(phone);
        if(patient == null){
            log.warn("患者登录失败，手机号未注册：{}", phone);
            loginAttemptService.loginFailed(phone);
            throw new AccountNotFoundException();
        }

        //密码校验
        if(!PasswordUtil.matches(password, patient.getPassword())){
            log.warn("患者登录失败，密码错误：{}", phone);
            loginAttemptService.loginFailed(phone);
            throw new PasswordErrorException();
        }

        //判断账号状态
        if(StatusConstant.ACCOUNT_DISABLED.equals(patient.getStatus())){
            log.warn("患者登录失败，账号已被禁用：{}", phone);
            throw new AccountDisabledException();
        }

        log.info("患者登录成功：{}", phone);

        // 登录成功，清除失败计数
        loginAttemptService.loginSucceeded(phone);

        return patient;

    }

    /**
     * 修改个人信息
     * @param patientId 当前患者ID
     * @param dto 修改参数
     * @return
     */
    @Override
    public Map<String, Object> updateProfile(Long patientId, PatientUpdateDTO dto) {

        // 1. 查询患者是否存在
        Patient patient = patientAuthMapper.findById(patientId);
        if (patient == null) {
            log.warn("修改个人信息失败，患者不存在，ID：{}", patientId);
            throw new AccountNotFoundException();
        }

        // 2. 如果手机号变更，校验新手机号是否已被占用
        boolean phoneChanged = !dto.getPhone().equals(patient.getPhone());
        if (phoneChanged) {
            int count = patientAuthMapper.countByPhoneAndNotId(dto.getPhone(), patientId);
            if (count > 0) {
                log.warn("修改个人信息失败，手机号已被占用：{}", dto.getPhone());
                throw new PhoneAlreadyExistsException();
            }
        }

        // 3. 更新患者信息
        patient.setName(dto.getName());
        patient.setPhone(dto.getPhone());
        patient.setIdCard(dto.getIdCard());
        patient.setGender(dto.getGender());
        patientAuthMapper.updateById(patient);

        log.info("患者信息修改成功，ID：{}，手机号：{}", patientId, dto.getPhone());

        // 4. 如果手机号变更，生成新 Token
        String token = null;
        if (phoneChanged) {
            token = JwtUtil.createToken(patient.getId(), patient.getPhone(), RoleConstant.PATIENT);
            log.info("手机号变更，已生成新 Token");
        }

        // 5. 返回结果
        Map<String, Object> map = new HashMap<>();
        if (token != null) {
            map.put("token", token);
        }
        map.put("id", patient.getId());
        map.put("name", patient.getName());
        map.put("phone", patient.getPhone());
        return map;

    }

    /**
     * 修改密碼
     * @param patientId
     * @param dto
     */
    @Override
    public void updatePassword(Long patientId, PasswordUpdateDTO dto) {

        Patient patient = patientAuthMapper.findById(patientId);
        if (patient == null) {
            throw new AccountNotFoundException();
        }

        // 校验旧密码
        if (!PasswordUtil.matches(dto.getOldPassword(), patient.getPassword())) {
            throw new PasswordErrorException();
        }

        // 校验新密码与确认密码一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordConfirmException();
        }

        // 校验新旧密码不同
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new PasswordSameException();
        }

        // 加密并更新
        patientAuthMapper.updatePassword(patientId, PasswordUtil.encode(dto.getNewPassword()));
        log.info("患者密码修改成功，ID：{}", patientId);
    }

}
