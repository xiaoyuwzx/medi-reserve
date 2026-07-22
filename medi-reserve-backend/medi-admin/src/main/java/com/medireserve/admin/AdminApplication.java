package com.medireserve.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.medireserve")
@MapperScan(basePackages = {"com.medireserve.common.mapper", "com.medireserve.admin.mapper"})
@EnableAsync  // 开启异步支持
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}