package com.medireserve.patient.controller;

import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.LoginDTO;
import com.medireserve.common.dto.PasswordUpdateDTO;
import com.medireserve.common.dto.PatientRegisterDTO;
import com.medireserve.common.dto.PatientUpdateDTO;
import com.medireserve.common.entity.Patient;
import com.medireserve.common.result.Result;
import com.medireserve.common.utils.JwtUtil;
import com.medireserve.patient.service.PatientAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 *  患者端认证：登录、注册
 */
@Slf4j
@RestController
@RequestMapping("/patient")
@Tag(name = "患者端 - 认证管理", description = "患者登录、注册相关接口")
public class PatientAuthController {

    @Autowired
    private PatientAuthService patientAuthService;

    /**
     * 患者注册
     * @param registerDTO
     * @return
     */
    @PostMapping("/register")
    @LogOperation(module = "认证管理", operation = "患者注册")
    @Operation(summary = "患者注册", description = "填写个人信息注册患者账号")
    public Result<Map<String, Object>> register(@RequestBody @Valid PatientRegisterDTO registerDTO){

        log.info("患者注册中... 手机号：{}", registerDTO.getPhone());

        // 直接调用 Service，如果出错会抛出异常，由全局处理器统一处理
        Patient patient = patientAuthService.register(registerDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("id", patient.getId());
        map.put("name", patient.getName());
        map.put("phone", patient.getPhone());

        log.info("患者账号注册成功：{}", map);

        return Result.success(MessageConstant.REGISTER_SUCCESS, map);

    }

    /**
     * 患者登录
     * @param loginDTO
     * @return
     */
    @PostMapping("/login")
    @LogOperation(module = "认证管理", operation = "患者登录")
    @Operation(summary = "患者登录", description = "手机号 + 密码，成功登录后返回 JWT 令牌")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginDTO loginDTO){

        log.info("患者登录中...：{}", loginDTO.getUsername());

        // 直接调用 Service，如果出错会抛出异常，由全局处理器统一处理
        Patient patient = patientAuthService.login(loginDTO.getUsername(), loginDTO.getPassword());

        //登录成功，生成 JWT 令牌
        String token = JwtUtil.createToken(
                patient.getId(),
                patient.getPhone(),
                RoleConstant.PATIENT
        );

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("id", patient.getId());
        map.put("name", patient.getName());
        map.put("phone", patient.getPhone());

        log.info("患者登录成功：{}", map);

        return Result.success(MessageConstant.LOGIN_SUCCESS, map);

    }

    /**
     * 修改个人信息
     */
    @PutMapping("/profile")
    @RequireRole(RoleConstant.PATIENT)
    @LogOperation(module = "认证管理", operation = "修改患者信息")
    @Operation(summary = "修改个人信息", description = "修改姓名、手机号、性别、身份证号")
    public Result<Map<String, Object>> updateProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody @Valid PatientUpdateDTO dto) {

        log.info("修改个人信息，患者ID：{}", userId);

        Map<String, Object> result = patientAuthService.updateProfile(userId, dto);

        log.info("个人信息修改成功，患者ID：{}", userId);

        return Result.success(MessageConstant.UPDATE_SUCCESS, result);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @RequireRole(RoleConstant.PATIENT)
    @LogOperation(module = "认证管理", operation = "修改患者密码")
    @Operation(summary = "修改密码", description = "验证旧密码后更新为新密码")
    public Result<Void> updatePassword(
            @RequestAttribute("userId") Long userId,
            @RequestBody @Valid PasswordUpdateDTO dto) {

        log.info("修改密码，患者ID：{}", userId);
        patientAuthService.updatePassword(userId, dto);
        log.info("密码修改成功，患者ID：{}", userId);

        return Result.success(MessageConstant.PASSWORD_UPDATE_SUCCESS, null);
    }

}
