package com.medireserve.doctor.service;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.DailyTrendVO;
import com.medireserve.common.dto.DoctorEvaluationVO;
import com.medireserve.common.dto.DoctorStatisticsOverviewVO;
import com.medireserve.common.dto.RateTrendVO;

import java.util.List;

/**
 * 医生端数据统计服务接口
 */
public interface DoctorStatisticsService {

    /**
     * 获取总览统计
     */
    DoctorStatisticsOverviewVO getOverview(Long doctorId);

    /**
     * 获取近 N 天每日接诊趋势
     * @param days 天数，默认 7，最大 90
     */
    List<DailyTrendVO> getTrend(Long doctorId, int days);

    /**
     * 分页获取医生的评价列表
     */
    PageInfo<DoctorEvaluationVO> getEvaluations(Long doctorId, int page, int size);

    /**
     * 获取近 N 天每日评分和好评率趋势
     * @param doctorId 医生ID
     * @param days 天数（默认7，最大90）
     * @return 每日评分/好评率列表
     */
    List<RateTrendVO> getRateTrend(Long doctorId, int days);

}