package com.medireserve.patient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "ok");
        result.put("service", "medi-patient (患者端)");
        result.put("message", "服务已启动成功！");
        return result;
    }
}