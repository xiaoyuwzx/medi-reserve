package com.medireserve.doctor.controller;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.ScheduleCreateDTO;
import com.medireserve.common.dto.ScheduleQueryDTO;
import com.medireserve.common.entity.Schedule;
import com.medireserve.common.result.Result;
import com.medireserve.doctor.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排班管理业务接口
 */
@Slf4j
@RestController
@RequestMapping("/doctor")
@Tag(name = "医生端 - 排班管理", description = "医生设置排班、查询、停诊、删除")
public class ScheduleController {

    @Autowired
    ScheduleService scheduleService;

    /**
     * 获取推荐号源数
     * @param doctorId
     * @param scheduleDate
     * @param userInputMax
     * @return
     */
    @GetMapping("/schedules/recommend")
    @Operation(summary = "获取推荐号源数", description = "基于历史就诊数据，智能推荐号源数量（仅做参考，用户可自行修改）")
    public Result<Map<String, Object>> getRecommendedMaxCount(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate scheduleDate,
            @RequestParam(required = false, defaultValue = "20") int userInputMax   //非必须参数，默认值为20
    ){

        log.info("获取推荐号源数，医生ID：{}，日期：{}，用户基准值：{}", doctorId, scheduleDate, userInputMax);

        // 调用 Service 计算推荐值
        Integer recommended = scheduleService.getRecommendedMaxCount(doctorId, scheduleDate, userInputMax);

        Map<String, Object> map = new HashMap<>();
        map.put("userInputMax", userInputMax);  //用户输入的基准值
        map.put("recommendedMax", recommended); //算法推荐值
        map.put("difference", recommended - userInputMax);  //差值

        //计算变化百分比
        int percentChange = 0;
        if(userInputMax != 0){
            percentChange = (int)((recommended - userInputMax) * 100.0 / userInputMax);
        }
        map.put("percentChange", percentChange);

        return Result.success(map);

    }

    /**
     * 新增排班
     * @param scheduleCreateDTO
     * @return
     */
    @PostMapping("/schedules")
    @Operation(summary = "新增排班", description = "医生选择日期和时段，设置最大挂号数（系统会基于历史数据智能推荐）")
    public Result<Map<String, Object>> createSchedule(@RequestBody @Valid ScheduleCreateDTO scheduleCreateDTO){

        log.info("接收新增排班请求，医生ID：{}，日期：{}", scheduleCreateDTO.getDoctorId(), scheduleCreateDTO.getScheduleDate());

        // 直接调用 Service，如果出错会抛出异常，由全局处理器统一处理
        Schedule schedule = scheduleService.createSchedule(scheduleCreateDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("scheduleID", schedule.getId());
        map.put("doctorId", schedule.getDoctorId());
        map.put("scheduleDate", schedule.getScheduleDate());
        map.put("period", schedule.getPeriod());
        map.put("maxCount", schedule.getMaxCount());
        map.put("remainingCount", schedule.getRemainingCount());
        map.put("status", schedule.getStatus());

        log.info("新增排班成功：{}", map);

        return Result.success(MessageConstant.SCHEDULE_CREATE_SUCCESS, map);

    }

    /**
     * 查询医生排班列表
     * @param doctorId
     * @param scheduleQueryDTO
     * @return
     */
    @GetMapping("/schedules")
    @Operation(summary = "查询我的排班", description = "医生查看自己的排班列表，支持按日期范围筛选")
    public Result<List<Schedule>> listSchedules(@RequestParam Long doctorId, @Valid ScheduleQueryDTO scheduleQueryDTO){

        log.info("获取医生排班，医生ID：{}", doctorId);

        List<Schedule> list = scheduleService.listSchedule(doctorId, scheduleQueryDTO);

        return Result.success(list);

    }

    /**
     * 修改排班状态：停诊/恢复
     * @param id
     * @param status
     * @return
     */
    @PatchMapping("/schedules/{id}/status")
    @Operation(summary = "停诊/恢复排班", description = "传入 status=2 停诊，status=1 恢复")
    public Result<String> updateScheduleStatus(@PathVariable Long id, @RequestParam int status){

        log.info("更改排班状态，排班ID：{}，目标状态：{}", id, status);

        scheduleService.updateScheduleStatus(id, status);

        String msg = StatusConstant.SCHEDULE_STOPPED.equals(status)
                        ? MessageConstant.SCHEDULE_STOP_SUCCESS
                        : MessageConstant.SCHEDULE_RESUME_SUCCESS;

        return Result.success(msg);

    }

}
