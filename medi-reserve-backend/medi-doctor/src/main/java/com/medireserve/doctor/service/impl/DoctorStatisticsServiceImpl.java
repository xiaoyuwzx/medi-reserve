package com.medireserve.doctor.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.DailyTrendVO;
import com.medireserve.common.dto.DoctorEvaluationVO;
import com.medireserve.common.dto.DoctorStatisticsOverviewVO;
import com.medireserve.common.exception.DoctorNotFoundException;
import com.medireserve.common.mapper.DoctorAuthMapper;
import com.medireserve.doctor.mapper.DoctorStatisticsMapper;
import com.medireserve.doctor.service.DoctorStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 医生端数据统计服务实现
 */
@Slf4j
@Service
public class DoctorStatisticsServiceImpl implements DoctorStatisticsService {

    @Autowired
    private DoctorStatisticsMapper statisticsMapper;

    @Autowired
    private DoctorAuthMapper doctorAuthMapper;

    /**
     * 获取总览统计
     * @param doctorId
     * @return
     */
    @Override
    public DoctorStatisticsOverviewVO getOverview(Long doctorId) {
        // 校验医生是否存在
        if (doctorAuthMapper.findById(doctorId) == null) {
            throw new DoctorNotFoundException();
        }

        // 并行查询各项数据（可优化为单次查询，但可读性优先）
        Long totalPatients = statisticsMapper.countTotalPatients(doctorId);
        Long todayPatients = statisticsMapper.countTodayPatients(doctorId);
        BigDecimal avgScore = statisticsMapper.avgScore(doctorId);
        BigDecimal positiveRate = statisticsMapper.positiveRate(doctorId);
        Long evaluationCount = statisticsMapper.countEvaluations(doctorId);
        Long pendingConsultations = statisticsMapper.countPendingConsultations(doctorId);

        // 处理 null 值（无数据时返回默认值）
        if (totalPatients == null) totalPatients = 0L;
        if (todayPatients == null) todayPatients = 0L;
        if (avgScore == null) avgScore = BigDecimal.ZERO;
        if (positiveRate == null) positiveRate = BigDecimal.ZERO;
        if (evaluationCount == null) evaluationCount = 0L;
        if (pendingConsultations == null) pendingConsultations = 0L;

        DoctorStatisticsOverviewVO vo = new DoctorStatisticsOverviewVO();
        vo.setTotalPatients(totalPatients);
        vo.setTodayPatients(todayPatients);
        vo.setAvgScore(avgScore);
        vo.setPositiveRate(positiveRate);
        vo.setEvaluationCount(evaluationCount);
        vo.setPendingConsultations(pendingConsultations);

        log.info("医生 {} 统计总览查询完成", doctorId);
        return vo;
    }

    /**
     * 获取近 N 天每日接诊趋势
     * @param doctorId
     * @param days 天数，默认 7，最大 90
     * @return
     */
    @Override
    public List<DailyTrendVO> getTrend(Long doctorId, int days) {
        // 校验医生是否存在
        if (doctorAuthMapper.findById(doctorId) == null) {
            throw new DoctorNotFoundException();
        }

        // 限制天数
        if (days < 1) days = 1;
        if (days > 90) days = 90;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 查询数据库已有数据
        List<DailyTrendVO> dbList = statisticsMapper.selectDailyTrend(doctorId, startDate, endDate);

        // 补全缺失日期（某天无数据则 count=0）
        Map<LocalDate, DailyTrendVO> dateMap = dbList.stream()
                .collect(Collectors.toMap(DailyTrendVO::getDate, Function.identity()));

        List<DailyTrendVO> fullList = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DailyTrendVO vo = dateMap.getOrDefault(date, new DailyTrendVO());
            vo.setDate(date);
            if (!dateMap.containsKey(date)) {
                vo.setCount(0L);
            }
            fullList.add(vo);
        }

        log.info("医生 {} 趋势数据查询完成，共 {} 天", doctorId, fullList.size());
        return fullList;
    }

    /**
     * 分页获取医生的评价列表
     * @param doctorId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<DoctorEvaluationVO> getEvaluations(Long doctorId, int page, int size) {
        // 校验医生是否存在
        if (doctorAuthMapper.findById(doctorId) == null) {
            throw new DoctorNotFoundException();
        }

        // 分页查询
        PageHelper.startPage(page, size);
        List<DoctorEvaluationVO> list = statisticsMapper.selectEvaluations(doctorId);
        long total = statisticsMapper.countEvaluations(doctorId);

        // 构造 PageInfo
        PageInfo<DoctorEvaluationVO> pageInfo = new PageInfo<>(list);
        pageInfo.setTotal(total);

        log.info("医生 {} 评价列表查询完成，共 {} 条", doctorId, total);
        return pageInfo;
    }
}