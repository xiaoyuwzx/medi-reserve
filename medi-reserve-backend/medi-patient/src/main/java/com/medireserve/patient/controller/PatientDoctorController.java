package com.medireserve.patient.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.DepartmentVO;
import com.medireserve.common.dto.DoctorListQueryDTO;
import com.medireserve.common.dto.DoctorListVO;
import com.medireserve.common.dto.ScheduleCalendarVO;
import com.medireserve.common.result.Result;
import com.medireserve.patient.service.PatientDoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端 - 医生/排班查询服务
 * 提供科室列表、医生列表、排班日历等查询功能
 */
@Slf4j
@RestController
@RequestMapping("/patient")
@Tag(name = "患者端 - 号源展示", description = "患者浏览科室、医生、排班号源")
public class PatientDoctorController {

    @Autowired
    private PatientDoctorService patientDoctorService;

    /**
     * 获取科室列表
     * @return
     */
    @GetMapping("/departments")
    @Operation(summary = "获取科室列表", description = "获取所有科室及医生数量，用于前端下拉筛选")
    public Result<List<DepartmentVO>> getAllDepartments(){

        log.info("获取科室列表");

        List<DepartmentVO> list = patientDoctorService.getAllDepartments();

        return Result.success(list);

    }

    /**
     * 分页查询医生列表
     * @param doctorListQueryDTO
     * @return
     */
    @GetMapping("/doctors")
    @Operation(summary = "分页查询医生列表", description = "支持按科室删选、关键词搜索(名字/擅长)")
    public Result<PageInfo<DoctorListVO>> getDoctorList(@Valid DoctorListQueryDTO doctorListQueryDTO){

        log.info("查询医生列表，科室：{}，关键词：{}，页码：{}，每页：{}",
                doctorListQueryDTO.getDepartment(), doctorListQueryDTO.getKeyword(), doctorListQueryDTO.getPage(), doctorListQueryDTO.getSize());

        //参数校验
        if(doctorListQueryDTO.getPage() == null || doctorListQueryDTO.getPage() < 1){
            doctorListQueryDTO.setPage(1);
        }
        if(doctorListQueryDTO.getSize() == null || doctorListQueryDTO.getSize() < 1 || doctorListQueryDTO.getSize() > 100){
            doctorListQueryDTO.setSize(10);
        }

        PageInfo<DoctorListVO> pageInfo = patientDoctorService.getDoctorList(doctorListQueryDTO);

        return Result.success(pageInfo);

    }

    /**
     * 获取医生排班日历
     * @param doctorId
     * @return
     */
    @GetMapping("/doctors/{doctorId}/schedules")
    @Operation(summary = "获取医生排班日历", description = "查看某医生未来7天的排班号源情况")
    public Result<List<ScheduleCalendarVO>> getScheduleCalendar(@PathVariable Long doctorId){

        log.info("获取医生排班日历，医生ID：{}", doctorId);

        List<ScheduleCalendarVO> calendarList = patientDoctorService.getScheduleCalendar(doctorId);

        return Result.success(calendarList);

    }

}
