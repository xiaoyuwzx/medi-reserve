package com.medireserve.doctor.controller;

import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.dto.*;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.result.Result;
import com.medireserve.common.utils.JwtUtil;
import com.medireserve.doctor.service.DoctorAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 *  医生端认证：登录、注册
 */
@Slf4j
@RestController
@RequestMapping("/doctor")
@Tag(name = "医生端 - 认证管理", description = "医生登录、注册相关接口")
public class DoctorAuthController {

    @Autowired
    private DoctorAuthService doctorAuthService;

    /**
     * 医生注册
     * @param registerDTO
     * @return
     */
    @PostMapping("/register")
    @LogOperation(module = "认证管理", operation = "医生注册")
    @Operation(summary = "医生注册", description = "填写个人信息注册医生账号，提交后进入待审核状态")
    public Result<Map<String, Object>> register(@RequestBody @Valid DoctorRegisterDTO registerDTO){

        log.info("医生注册中... 手机号：{}",registerDTO.getPhone());

        // 直接调用 Service，如果出错会抛出异常，由全局处理器统一处理
        Doctor doctor = doctorAuthService.register(registerDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("id", doctor.getId());
        map.put("name", doctor.getName());
        map.put("phone", doctor.getPhone());
        map.put("auditStatus", StatusConstant.AUDIT_PENDING);  // 返回 0
        map.put("auditStatusText", MessageConstant.DOCTOR_AUDIT_PENDING);  // 返回 "待审核"

        log.info("医生账号注册成功：{}", map);

        return Result.success(MessageConstant.DOCTOR_REGISTER_PENDING, map);

    }

    /**
     * 医生登录
     * @param loginDTO
     * @return
     */
    @PostMapping("/login")
    @LogOperation(module = "认证管理", operation = "医生登录")
    @Operation(summary = "医生登录", description = "手机号 + 密码登录，成功后返回 JWT 令牌")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginDTO loginDTO){

        log.info("医生登录中... 手机号：{}", loginDTO.getUsername());

        // 直接调用 Service，如果出错会抛出异常，由全局处理器统一处理
        Doctor doctor = doctorAuthService.login(loginDTO.getUsername(), loginDTO.getPassword());

        //登录成功，生成JWT令牌
        String token = JwtUtil.createToken(
                doctor.getId(),
                doctor.getPhone(),
                RoleConstant.DOCTOR
        );

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("id", doctor.getId());
        map.put("name", doctor.getName());
        map.put("phone", doctor.getPhone());

        log.info("医生登录成功，ID：{}，手机号：{}", doctor.getId(), doctor.getPhone());

        return Result.success(MessageConstant.LOGIN_SUCCESS, map);

    }

    /**
     * 修改密码
     * @param userId
     * @param dto
     * @return
     */
    @PutMapping("/password")
    @RequireRole(RoleConstant.DOCTOR)
    @LogOperation(module = "认证管理", operation = "修改医生密码")
    @Operation(summary = "修改密码", description = "验证旧密码后更新为新密码")
    public Result<Void> updatePassword(
            @RequestAttribute("userId") Long userId,
            @RequestBody @Valid PasswordUpdateDTO dto) {

        log.info("修改密码，医生ID：{}", userId);

        doctorAuthService.updatePassword(userId, dto);

        log.info("密码修改成功，医生ID：{}", userId);

        return Result.success(MessageConstant.PASSWORD_UPDATE_SUCCESS, null);

    }

    /**
     * 修改医生个人信息
     * 普通信息（姓名、手机号、性别、身份证号）立即生效
     * 证件信息（执业证书、资格证）提交审核，需管理员审批
     */
    @PutMapping("/profile")
    @RequireRole(RoleConstant.DOCTOR)
    @Operation(summary = "修改个人信息",
            description = "普通信息立即生效，证件信息提交审核（需管理员审批）")
    public Result<Map<String, Object>> updateProfile(
            @RequestAttribute("userId") Long doctorId,
            @RequestBody @Valid DoctorUpdateDTO dto) {
        log.info("修改个人信息，医生ID：{}", doctorId);
        Map<String, Object> result = doctorAuthService.updateProfile(doctorId, dto);
        log.info("个人信息修改成功，医生ID：{}，证件已提交审核", doctorId);
        return Result.success(MessageConstant.UPDATE_SUCCESS, result);
    }

    /**
     * 查询医生证件审核状态
     */
    @GetMapping("/profile/audit-status")
    @RequireRole(RoleConstant.DOCTOR)
    @Operation(summary = "查询证件审核状态",
            description = "返回当前证件审核状态：待审核/已通过/已驳回/未提交")
    public Result<DoctorAuditInfoVO> getAuditStatus(
            @RequestAttribute("userId") Long doctorId) {
        log.info("查询证件审核状态，医生ID：{}", doctorId);
        DoctorAuditInfoVO vo = doctorAuthService.getAuditStatus(doctorId);
        return Result.success(vo);
    }

}
