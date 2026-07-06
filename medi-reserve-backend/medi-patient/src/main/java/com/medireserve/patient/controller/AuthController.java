package com.medireserve.patient.controller;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.LoginDTO;
import com.medireserve.common.dto.PatientRegisterDTO;
import com.medireserve.common.entity.Patient;
import com.medireserve.common.result.Result;
import com.medireserve.common.utils.JwtUtil;
import com.medireserve.patient.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 *  患者端认证：登录、注册
 */
@Slf4j
@RestController
@RequestMapping("/patient")
@Tag(name = "患者端 - 认证管理", description = "患者登录、注册相关接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 患者注册
     * @param registerDTO
     * @return
     */
    @PostMapping("/register")
    @Operation(summary = "患者注册", description = "填写个人信息注册患者账号")
    public Result<Map<String, Object>> register(@RequestBody @Valid PatientRegisterDTO registerDTO){

        log.info("患者注册中... 手机号：{}", registerDTO.getPhone());

        // 直接调用 Service，如果出错会抛出异常，由全局处理器统一处理
        Patient patient = authService.register(registerDTO);

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
    @Operation(summary = "患者登录", description = "手机号 + 密码，成功登录后返回 JWT 令牌")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginDTO loginDTO){

        log.info("患者登录中...：{}", loginDTO.getUsername());

        // 直接调用 Service，如果出错会抛出异常，由全局处理器统一处理
        Patient patient = authService.login(loginDTO.getUsername(), loginDTO.getPassword());

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

}