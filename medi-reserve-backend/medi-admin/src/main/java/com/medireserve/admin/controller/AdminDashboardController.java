package com.medireserve.admin.controller;

import com.medireserve.admin.service.AdminDashboardService;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.*;
import com.medireserve.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端数据统计看板接口
 * 所有接口均需登录且具备超级管理员权限
 */
@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "管理端 - 数据统计看板", description = "提供各类运营统计指标")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService dashboardService;

    /**
     * 总览统计
     * @return
     */
    @GetMapping("/overview")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "总览统计", description = "返回今日关键指标及总量")
    public Result<DashboardOverviewVO> getOverview() {

        log.info("接收总览统计请求");

        DashboardOverviewVO data = dashboardService.getOverview();

        return Result.success(data);

    }

    /**
     * 趋势数据
     * @param days
     * @return
     */
    @GetMapping("/trend")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "趋势数据", description = "近N天每日挂号/支付/收入趋势")
    public Result<List<TrendDataVO>> getTrend(
            @Parameter(description = "查询天数，默认7，最大90")
            @RequestParam(defaultValue = "7") int days) {

        log.info("接收趋势请求，days={}", days);

        List<TrendDataVO> list = dashboardService.getTrendData(days);

        return Result.success(list);

    }

    /**
     * 科室排行
     * @param limit
     * @return
     */
    @GetMapping("/department-ranking")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "科室排行", description = "按挂号量降序返回科室排名")
    public Result<List<DepartmentRankingVO>> getDepartmentRanking(
            @Parameter(description = "返回前N条，默认10，最大50")
            @RequestParam(defaultValue = "10") int limit) {

        log.info("接收科室排行请求，limit={}", limit);

        List<DepartmentRankingVO> list = dashboardService.getDepartmentRanking(limit);

        return Result.success(list);

    }

    /**
     * 医生排行
     * @param limit
     * @param sortBy
     * @return
     */
    @GetMapping("/doctor-ranking")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "医生排行", description = "按挂号量或评分排序医生")
    public Result<List<DoctorRankingVO>> getDoctorRanking(
            @Parameter(description = "返回前N条，默认10，最大50")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "排序字段：appointment（挂号量）或 score（评分）")
            @RequestParam(defaultValue = "appointment") String sortBy) {

        log.info("接收医生排行请求，limit={}, sortBy={}", limit, sortBy);

        List<DoctorRankingVO> list = dashboardService.getDoctorRanking(limit, sortBy);

        return Result.success(list);

    }

    /**
     * 状态分布
     * @return
     */
    @GetMapping("/status-distribution")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "预约状态分布", description = "统计各状态预约数量")
    public Result<List<StatusDistributionVO>> getStatusDistribution() {

        log.info("接收状态分布请求");

        List<StatusDistributionVO> list = dashboardService.getStatusDistribution();

        return Result.success(list);

    }

}
