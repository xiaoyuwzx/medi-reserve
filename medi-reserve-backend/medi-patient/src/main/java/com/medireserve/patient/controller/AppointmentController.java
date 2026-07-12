package com.medireserve.patient.controller;

import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.AppointmentCreateDTO;
import com.medireserve.common.dto.ScheduleDetailVO;
import com.medireserve.common.entity.Appointment;
import com.medireserve.common.result.Result;
import com.medireserve.patient.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 预约挂号：挂号下单、支付、查询排班等
 */
@Slf4j
@RestController
@RequestMapping("/patient")
@Tag(name = "患者端 - 预约挂号", description = "挂号下单、支付、查询排班")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 查询排班详细
     * @param scheduleId
     * @return
     */
    @GetMapping("/schedules/{scheduleId}")
    @Operation(summary = "查询排班详细", description = "挂号前确定排班信息(日期、时段、医生、剩余号源)")
    public Result<ScheduleDetailVO> getScheduleDetail(@PathVariable Long scheduleId){

        log.info("查询排班详细，排班ID：{}", scheduleId);

        ScheduleDetailVO scheduleDetailVO = appointmentService.getScheduleDetail(scheduleId);

        return Result.success(scheduleDetailVO);

    }

    /**
     * 创建预约(下单)
     * @param appointmentCreateDTO
     * @param patientId
     * @return
     */
    @PostMapping("/appointments")
    @RequireRole(RoleConstant.PATIENT)
    @Operation(summary = "创建预约(下单)", description = "患者选择排班，扣减号源，生成待支付预约单")
    public Result<Map<String, Object>> createAppointment(
            @RequestBody @Valid AppointmentCreateDTO appointmentCreateDTO,
            @RequestAttribute("userId") Long patientId){

        log.info("创建预约，患者ID：{}，排班ID：{}", patientId, appointmentCreateDTO.getScheduleId());

        Appointment appointment = appointmentService.createAppointment(patientId, appointmentCreateDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("appointmentId", appointment.getId());
        map.put("appointmentNo", appointment.getAppointmentNo());
        map.put("status", appointment.getStatus());
        map.put("statusText", MessageConstant.STATUS_PENDING_PAY);
        map.put("payDeadline", MessageConstant.PAY_DEADLINE_TEXT);

        log.info("预约创建成功，预约ID：{}", appointment.getId());

        return Result.success(MessageConstant.APPOINTMENT_CREATE_SUCCESS, map);

    }

    @PostMapping("/appointments/{appointmentId}/pay")
    @RequireRole(RoleConstant.PATIENT)
    @Operation(summary = "模拟支付", description = "模拟微信支付回调，将预约状态改为已支付")
    public Result<String> payAppointment(
            @PathVariable Long appointmentId,
            @RequestAttribute("userId") Long patientId){

        log.info("模拟支付，预约ID：{}，患者ID：{}", appointmentId, patientId);

        appointmentService.payAppointment(appointmentId, patientId);

        return Result.success(MessageConstant.APPOINTMENT_PAY_SUCCESS);

    }

}
