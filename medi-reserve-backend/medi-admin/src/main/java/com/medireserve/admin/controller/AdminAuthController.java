package com.medireserve.admin.controller;

import com.medireserve.admin.service.AdminAuthService;
import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.AdminRegisterDTO;
import com.medireserve.common.dto.LoginDTO;
import com.medireserve.common.dto.PasswordUpdateDTO;
import com.medireserve.common.entity.Admin;
import com.medireserve.common.result.Result;
import com.medireserve.common.utils.JwtUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端认证：登录
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员 - 认证管理", description = "管理员登录、注册相关接口")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    /**
     * 管理员注册
     * @param registerDTO
     * @param currentRole
     * @return
     */
    @PostMapping("/register")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @LogOperation(module = "管理员管理", operation = "添加管理员")
    @Operation(summary = "管理员注册", description = "创建管理员账号（仅限超级管理员操作）")
    public Result<Map<String, Object>> register(
            @RequestBody @Valid AdminRegisterDTO registerDTO,
            @RequestAttribute("role") String currentRole
    ){

        log.info("管理员注册请求，当前操作者：{} , 目标用户名：{}", currentRole, registerDTO.getUsername());

        // 调用 Service，传入当前用户角色进行校验
        Admin admin = adminAuthService.register(registerDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("id", admin.getId());
        map.put("username", admin.getUsername());
        map.put("phone", admin.getPhone());
        map.put("name", admin.getName());
        map.put("role", admin.getRole());

        log.info("管理员注册成功，用户名：{}，ID：{}", admin.getUsername(), admin.getId());

        return Result.success(MessageConstant.REGISTER_SUCCESS, map);

    }

    /**
     * 管理员登录
     * @param loginDTO
     * @return
     */
    @PostMapping("/login")
    @LogOperation(module = "登录认证", operation = "管理员登录")
    @Operation(summary = "管理员登录", description = "用户名 + 密码登录，成功后返回 JWT 令牌")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginDTO loginDTO){

        log.info("管理员登录中... , 用户名：{}", loginDTO.getUsername());

        // 调用 Service，进行校验
        Admin admin = adminAuthService.login(loginDTO.getUsername(), loginDTO.getPassword());

        //生成角色名称
        String roleName = RoleConstant.getRoleName(admin.getRole());

        //生成 JWT 令牌
        String token = JwtUtil.createToken(
                admin.getId(),
                admin.getUsername(),
                roleName
        );

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("id", admin.getId());
        map.put("name", admin.getName());
        map.put("username", admin.getUsername());
        map.put("phone", admin.getPhone());
        map.put("role", admin.getRole());

        log.info("管理员登录成功，用户名：{}，角色：{}", admin.getUsername(), roleName);

        return Result.success(MessageConstant.LOGIN_SUCCESS, map);

    }

    /**
     * 获取管理员列表
     */
    @GetMapping("/list")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "管理员列表", description = "获取所有管理员账号列表（仅超级管理员）")
    public Result<PageInfo<Admin>> listAdmins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageInfo<Admin> pageInfo = adminAuthService.getAdminList(page, size);
        return Result.success(pageInfo);
    }

    /**
     * 修改管理员状态（禁用/启用）
     */
    @PatchMapping("/{id}/status")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @LogOperation(module = "管理员管理", operation = "修改管理员状态")
    @Operation(summary = "修改管理员状态", description = "禁用或启用管理员账号")
    public Result<String> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body,
            @RequestAttribute("userId") Long currentAdminId) {

        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(MessageConstant.INVALID_STATUS);
        }

        // Service 层会校验不能禁用自己
        adminAuthService.updateAdminStatus(id, status, currentAdminId);
        String msg = status == 0 ? MessageConstant.ACCOUNT_DISABLED : MessageConstant.ACCOUNT_ENABLED;
        return Result.success(msg);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @LogOperation(module = "管理员管理", operation = "修改管理员密码")
    @Operation(summary = "修改密码", description = "验证旧密码后更新为新密码")
    public Result<Void> updatePassword(
            @RequestAttribute("userId") Long userId,
            @RequestBody @Valid PasswordUpdateDTO dto) {

        log.info("修改密码，管理员ID：{}", userId);
        adminAuthService.updatePassword(userId, dto);
        log.info("密码修改成功，管理员ID：{}", userId);

        return Result.success(MessageConstant.PASSWORD_UPDATE_SUCCESS, null);
    }

}
