package com.medireserve.doctor.service.impl;

import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.DoctorAuditInfoVO;
import com.medireserve.common.dto.DoctorRegisterDTO;
import com.medireserve.common.dto.DoctorUpdateDTO;
import com.medireserve.common.dto.PasswordUpdateDTO;
import com.medireserve.common.entity.Department;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.DoctorAudit;
import com.medireserve.common.entity.Title;
import com.medireserve.common.exception.*;
import com.medireserve.common.service.LoginAttemptService;
import com.medireserve.common.utils.JwtUtil;
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
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    public void updatePassword(Long doctorId, PasswordUpdateDTO dto) {

        Doctor doctor = doctorAuthMapper.findById(doctorId);
        if (doctor == null) {
            throw new AccountNotFoundException();
        }

        if (!PasswordUtil.matches(dto.getOldPassword(), doctor.getPassword())) {
            throw new PasswordErrorException();
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        doctorAuthMapper.updatePassword(doctorId, PasswordUtil.encode(dto.getNewPassword()));
        log.info("医生密码修改成功，ID：{}", doctorId);
    }

    /**
     * 更新医生个人信息
     * 普通信息立即生效，证件信息提交审核
     */
    @Transactional
    @Override
    public Map<String, Object> updateProfile(Long doctorId, DoctorUpdateDTO dto) {
        // 1. 查询医生是否存在
        Doctor doctor = doctorAuthMapper.findById(doctorId);
        if (doctor == null) {
            log.warn("修改个人信息失败，医生不存在，ID：{}", doctorId);
            throw new AccountNotFoundException();
        }

        // 2. 如果手机号变更，校验新手机号是否已被占用
        boolean phoneChanged = !dto.getPhone().equals(doctor.getPhone());
        if (phoneChanged) {
            int count = doctorAuthMapper.countByPhoneAndNotId(dto.getPhone(), doctorId);
            if (count > 0) {
                log.warn("修改个人信息失败，手机号已被占用：{}", dto.getPhone());
                throw new PhoneAlreadyExistsException();
            }
        }

        // 3. 更新 doctor 表基本信息（立即生效）
        doctor.setName(dto.getName());
        doctor.setPhone(dto.getPhone());
        doctor.setIdCard(dto.getIdCard());
        doctor.setGender(dto.getGender());
        doctorAuthMapper.updateById(doctor);

        // 4. 更新 doctor_audit 表（专业信息立即生效 + 证件提交审核）
        int rows;
        if (StringUtils.hasText(dto.getCertificateUrl()) || StringUtils.hasText(dto.getQualificationUrl())) {
            // 有证件变更 → 提交审核
            rows = doctorAuditMapper.updateProfileAndSubmitCert(
                    doctorId,
                    dto.getSpecialty(),
                    dto.getIntroduction(),
                    dto.getCertificateUrl(),
                    dto.getQualificationUrl()
            );
        } else {
            // 无证件变更 → 只更新专业信息，不触发审核
            rows = doctorAuditMapper.updateProfileOnly(
                    doctorId,
                    dto.getSpecialty(),
                    dto.getIntroduction()
            );
        }
        if (rows == 0) {
            log.warn("医生审核资料不存在，医生ID：{}", doctorId);
            throw new BusinessException("医生资料不存在，请联系管理员");
        }

        log.info("医生信息修改成功，ID：{}，手机号：{}，证件已提交审核", doctorId, dto.getPhone());

        // 5. 如果手机号变更，生成新 Token
        String token = null;
        if (phoneChanged) {
            token = JwtUtil.createToken(doctor.getId(), doctor.getPhone(), "DOCTOR");
            log.info("手机号变更，已生成新 Token");
        }

        // 6. 返回结果
        Map<String, Object> resultMap = new HashMap<>();
        if (token != null) {
            resultMap.put("token", token);
        }
        resultMap.put("id", doctor.getId());
        resultMap.put("name", doctor.getName());
        resultMap.put("phone", doctor.getPhone());
        resultMap.put("gender", doctor.getGender());
        resultMap.put("idCard", doctor.getIdCard());
        resultMap.put("specialty", dto.getSpecialty());
        resultMap.put("introduction", dto.getIntroduction());
        // 注意：证件不立即生效，所以返回当前生效的证件，而不是新提交的
        // 前端可以通过 getAuditStatus 查询审核状态

        return resultMap;
    }

    /**
     * 查询医生证件审核状态
     */
    @Override
    public DoctorAuditInfoVO getAuditStatus(Long doctorId) {
        DoctorAudit audit = doctorAuditMapper.findByDoctorId(doctorId);
        if (audit == null) {
            throw new BusinessException("医生资料不存在");
        }

        DoctorAuditInfoVO vo = new DoctorAuditInfoVO();
        vo.setDoctorId(doctorId);
        vo.setCertificateUrl(audit.getCertificateUrl());
        vo.setQualificationUrl(audit.getQualificationUrl());
        vo.setPendingCertificateUrl(audit.getPendingCertificateUrl());
        vo.setPendingQualificationUrl(audit.getPendingQualificationUrl());
        vo.setCertAuditStatus(audit.getCertAuditStatus());
        vo.setCertAuditRemark(audit.getCertAuditRemark());
        vo.setCertAuditTime(audit.getCertAuditTime());
        vo.setSpecialty(audit.getSpecialty());
        vo.setIntroduction(audit.getIntroduction());

        // 设置状态描述
        if (audit.getCertAuditStatus() == null) {
            vo.setCertAuditStatusText("未提交");
        } else {
            switch (audit.getCertAuditStatus()) {
                case 0:
                    vo.setCertAuditStatusText("审核中");
                    break;
                case 1:
                    vo.setCertAuditStatusText("已通过");
                    break;
                case 2:
                    vo.setCertAuditStatusText("已驳回");
                    break;
                default:
                    vo.setCertAuditStatusText("未知");
            }
        }

        return vo;
    }

}
