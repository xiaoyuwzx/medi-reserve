package com.medireserve.doctor.controller;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.dto.DoctorRegisterDTO;
import com.medireserve.common.dto.LoginDTO;
import com.medireserve.common.dto.PasswordUpdateDTO;
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
     */
    @PutMapping("/password")
    @RequireRole(RoleConstant.DOCTOR)
    @Operation(summary = "修改密码", description = "验证旧密码后更新为新密码")
    public Result<Void> updatePassword(
            @RequestAttribute("userId") Long userId,
            @RequestBody @Valid PasswordUpdateDTO dto) {

        log.info("修改密码，医生ID：{}", userId);
        doctorAuthService.updatePassword(userId, dto);
        log.info("密码修改成功，医生ID：{}", userId);

        return Result.success("密码修改成功，请重新登录", null);
    }

}
