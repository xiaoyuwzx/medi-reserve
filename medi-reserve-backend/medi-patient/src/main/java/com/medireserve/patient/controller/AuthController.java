package com.medireserve.patient.controller;

import com.medireserve.common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    // 模拟登录：校验用户名密码，成功后返回令牌
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username,
                                     @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();

        // TODO: 此处应改为查询数据库验证账号密码
        if ("admin".equals(username) && "123456".equals(password)) {
            // 假设用户 ID 为 1，角色为 PATIENT（实际应从数据库中查询）
            Long userId = 1L;
            String role = "PATIENT";

            // 生成令牌（传入 id、name、role）
            String token = jwtUtil.createToken(userId, username, role);

            log.info("用户 {} 登录成功，生成令牌", username);

            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("token", token);
            // 可选：同时返回用户信息，方便前端使用
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", userId);
            userInfo.put("username", username);
            userInfo.put("role", role);
            result.put("userInfo", userInfo);
        } else {
            log.warn("登录失败，用户名或密码错误: {}", username);
            result.put("code", 401);
            result.put("msg", "用户名或密码错误");
        }
        return result;
    }
}