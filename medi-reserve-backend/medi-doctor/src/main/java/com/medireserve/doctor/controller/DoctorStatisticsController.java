package com.medireserve.doctor.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.DailyTrendVO;
import com.medireserve.common.dto.DoctorEvaluationVO;
import com.medireserve.common.dto.DoctorStatisticsOverviewVO;
import com.medireserve.common.result.Result;
import com.medireserve.doctor.service.DoctorStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生端 - 数据统计看板
 * 提供个人接诊数据总览、趋势、评价列表
 */
@Slf4j
@RestController
@RequestMapping("/doctor/statistics")
@RequireRole(RoleConstant.DOCTOR)
@Tag(name = "医生端 - 数据统计", description = "医生个人运营数据")
public class DoctorStatisticsController {

    @Autowired
    private DoctorStatisticsService statisticsService;

    /**
     * 总览统计
     */
    @GetMapping("/overview")
    @Operation(summary = "总览统计", description = "返回总接诊数、好评率、平均评分、今日接诊数、待处理问诊数")
    public Result<DoctorStatisticsOverviewVO> getOverview(@RequestAttribute("userId") Long doctorId) {

        log.info("医生 {} 请求总览统计", doctorId);

        DoctorStatisticsOverviewVO vo = statisticsService.getOverview(doctorId);

        return Result.success(vo);

    }

    /**
     * 趋势数据
     */
    @GetMapping("/trend")
    @Operation(summary = "每日接诊趋势", description = "近 N 天每日接诊量，默认 7 天，最大 90 天")
    public Result<List<DailyTrendVO>> getTrend(
            @RequestAttribute("userId") Long doctorId,
            @Parameter(description = "天数，默认 7，最大 90")
            @RequestParam(defaultValue = "7") int days) {

        log.info("医生 {} 请求趋势数据，天数：{}", doctorId, days);

        List<DailyTrendVO> list = statisticsService.getTrend(doctorId, days);

        return Result.success(list);

    }

    /**
     * 评价列表（分页）
     */
    @GetMapping("/evaluations")
    @Operation(summary = "评价列表", description = "分页获取患者对医生的评价")
    public Result<PageInfo<DoctorEvaluationVO>> getEvaluations(
            @RequestAttribute("userId") Long doctorId,
            @Parameter(description = "页码，默认 1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认 10，最大 50")
            @RequestParam(defaultValue = "10") int size) {

        log.info("医生 {} 请求评价列表，页码：{}，每页：{}", doctorId, page, size);

        // 参数边界校验
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 50) size = 50;

        PageInfo<DoctorEvaluationVO> pageInfo = statisticsService.getEvaluations(doctorId, page, size);

        return Result.success(pageInfo);

    }
}