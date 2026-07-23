package com.medireserve.patient.controller;

import com.medireserve.common.dto.DepartmentVO;
import com.medireserve.common.entity.Title;
import com.medireserve.common.result.Result;
import com.medireserve.patient.service.PatientDoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/patient/dict")
@Tag(name = "公共字典：获取所有科室、职称信息", description = "科室、职称下拉列表")
public class CommonDictController {

    @Autowired
    private PatientDoctorService patientDoctorService;

    @GetMapping("/departments")
    @Operation(summary = "获取科室列表")
    public Result<List<DepartmentVO>> getDepartments() {
        log.info("获取科室列表");
        return Result.success(patientDoctorService.getAllDepartments());
    }

    @GetMapping("/titles")
    @Operation(summary = "获取职称列表")
    public Result<List<Title>> getTitles() {
        log.info("获取职称列表");
        return Result.success(patientDoctorService.getAllTitles());
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有字典数据：获取科室、职称列表")
    public Result<Map<String, Object>> getAll() {
        log.info("获取所有字典数据：获取科室、职称列表");
        Map<String, Object> data = new HashMap<>();
        data.put("departments", patientDoctorService.getAllDepartments());
        data.put("titles", patientDoctorService.getAllTitles());
        return Result.success(data);
    }
}