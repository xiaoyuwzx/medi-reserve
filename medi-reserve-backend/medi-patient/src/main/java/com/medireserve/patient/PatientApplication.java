package com.medireserve.patient;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.medireserve")
@EnableScheduling
@EnableAsync
@MapperScan(basePackages = {"com.medireserve.common.mapper", "com.medireserve.patient.mapper"})
public class PatientApplication {
    public static void main(String[] args) {
        SpringApplication.run(PatientApplication.class, args);
    }
}