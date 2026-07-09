package com.medireserve.patient.controller;

import com.medireserve.common.dto.ScheduleDetailVO;
import com.medireserve.common.result.Result;
import com.medireserve.patient.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预约挂号：挂号下单、支付、查询排班等
 */
@Slf4j
@RestController
@RequestMapping("/patient")
@Tag(name = "患者端 - 预约挂号", description = "挂号下单、支付、查询排班")
public class AppointmentController {

    @Autowired
    AppointmentService appointmentService;

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

}
