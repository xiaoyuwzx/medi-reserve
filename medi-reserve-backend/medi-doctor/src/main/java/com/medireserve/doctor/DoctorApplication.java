package com.medireserve.doctor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.medireserve")
@MapperScan(basePackages = {"com.medireserve.common.mapper", "com.medireserve.doctor.mapper"})
public class DoctorApplication {
    public static void main(String[] args) {
        SpringApplication.run(DoctorApplication.class, args);
    }
}