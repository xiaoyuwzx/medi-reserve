package com.medireserve.admin.service;

import com.medireserve.common.dto.*;

import java.util.List;

/**
 * 管理端数据统计服务接口
 */
public interface AdminDashboardService {

    /**
     * 获取总览统计
     * @return
     */
    DashboardOverviewVO getOverview();

    /**
     * 获取近 N 天趋势数据
     * @param days
     * @return
     */
    List<TrendDataVO> getTrendData(int days);

    /**
     * 获取科室排行（默认取前 10）
     * @param limit
     * @return
     */
    List<DepartmentRankingVO> getDepartmentRanking(int limit);

    /**
     * 获取医生排行
     * @param limit 前 N 名
     * @param sortBy 排序依据：'appointment' 或 'score'
     */
    List<DoctorRankingVO> getDoctorRanking(int limit, String sortBy);

    /**
     * 获取预约状态分布
     * @return
     */
    List<StatusDistributionVO> getStatusDistribution();

}
