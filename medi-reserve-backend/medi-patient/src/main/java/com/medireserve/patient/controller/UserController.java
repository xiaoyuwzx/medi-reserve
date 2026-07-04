package com.medireserve.patient.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    // 方法1：通过 @RequestAttribute 获取
    @GetMapping("/info1")
    public Map<String, Object> getUserInfo1(@RequestAttribute("userId") Long userId,
                                            @RequestAttribute("username") String username,
                                            @RequestAttribute("role") String role) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("username", username);
        map.put("role", role);
        return map;
    }

    // 方法2：直接通过 HttpServletRequest 获取
    @GetMapping("/info2")
    public Map<String, Object> getUserInfo2(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", request.getAttribute("userId"));
        map.put("username", request.getAttribute("username"));
        map.put("role", request.getAttribute("role"));
        return map;
    }
}