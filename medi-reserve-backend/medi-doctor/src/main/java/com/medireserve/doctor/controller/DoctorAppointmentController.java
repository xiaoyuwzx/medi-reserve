package com.medireserve.doctor.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.AppointmentListVO;
import com.medireserve.common.mapper.AppointmentMapper;
import com.medireserve.common.result.Result;
import com.github.pagehelper.PageHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生端 - 预约查询
 * 查询医生今天需要问诊的患者列表
 */
@Slf4j
@RestController
@RequestMapping("/doctor")
@RequireRole(RoleConstant.DOCTOR)
@Tag(name = "医生端 - 在线问诊", description = "查询预约患者列表")
public class DoctorAppointmentController {

    @Autowired
    private AppointmentMapper appointmentMapper;

    /**
     * 查询医生的预约列表（已支付状态，默认今天）
     */
    @GetMapping("/appointments")
    @Operation(summary = "查询医生预约列表", description = "查询已支付的预约，默认查今天")
    public Result<PageInfo<AppointmentListVO>> getDoctorAppointments(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute("userId") Long doctorId) {

        log.info("医生查询预约列表，doctorId：{}，date：{}，status：{}", doctorId, date, status);

        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 20;
        // 默认查询已支付
        if (status == null) status = 1;

        PageHelper.startPage(page, size);
        List<AppointmentListVO> list = appointmentMapper.findDoctorAppointments(doctorId, date, status);
        int total = appointmentMapper.countDoctorAppointments(doctorId, date, status);

        PageInfo<AppointmentListVO> pageInfo = new PageInfo<>(list);
        pageInfo.setTotal(total);

        return Result.success(pageInfo);
    }
}