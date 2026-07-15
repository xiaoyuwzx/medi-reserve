package com.medireserve.patient.runner;

import com.medireserve.common.dto.DepartmentVO;
import com.medireserve.common.entity.Title;
import com.medireserve.patient.service.PatientDoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 缓存预热组件
 * 在应用启动后自动加载热点数据到缓存中
 */
@Slf4j
@Component
public class CacheWarmupRunner implements CommandLineRunner {

    @Autowired
    private PatientDoctorService patientDoctorService;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始缓存预热...");

        // 预热科室列表
        List<DepartmentVO> departments = patientDoctorService.getAllDepartments();
        log.info("预热科室列表，共 {} 条", departments.size());

        // 预热职称列表
        List<Title> titles = patientDoctorService.getAllTitles();
        log.info("预热职称列表，共 {} 条", titles.size());

        log.info("缓存预热完成");
    }
}