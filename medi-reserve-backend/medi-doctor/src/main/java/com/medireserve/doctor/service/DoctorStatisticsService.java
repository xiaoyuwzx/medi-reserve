package com.medireserve.doctor.service;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.DailyTrendVO;
import com.medireserve.common.dto.DoctorEvaluationVO;
import com.medireserve.common.dto.DoctorStatisticsOverviewVO;

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

}