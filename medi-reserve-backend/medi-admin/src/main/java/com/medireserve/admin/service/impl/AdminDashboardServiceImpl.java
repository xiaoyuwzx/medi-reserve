package com.medireserve.admin.service.impl;

import com.medireserve.admin.mapper.AdminDashboardMapper;
import com.medireserve.admin.service.AdminDashboardService;
import com.medireserve.common.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端数据统计服务实现
 * 负责汇聚各类统计数据，并处理默认值、空值
 */
@Slf4j
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    // 从配置文件读取默认挂号费（单位：元），若未配置则使用 10
    @Value("${medireserve.default-price:10}")
    private BigDecimal defaultPrice;

    @Autowired
    private AdminDashboardMapper dashboardMapper;

    /**
     * 获取总览统计
     * @return
     */
    @Override
    @Cacheable(value = "dashboard:overview", unless = "#result == null")
    public DashboardOverviewVO getOverview() {

        log.info("查询总览统计");

        // 调用 Mapper，传入 defaultPrice 计算收入
        DashboardOverviewVO vo = dashboardMapper.selectOverviewStatistics(defaultPrice);

        // 处理可能为 null 的字段，避免前端解析错误
        if (vo == null) {
            vo = new DashboardOverviewVO();
        }

        // 对 null 进行默认值填充（防止 NPE）
        if (vo.getTodayAppointments() == null) vo.setTodayAppointments(0L);
        if (vo.getTodayPaid() == null) vo.setTodayPaid(0L);
        if (vo.getTodayIncome() == null) vo.setTodayIncome(BigDecimal.ZERO);
        if (vo.getTotalAppointments() == null) vo.setTotalAppointments(0L);
        if (vo.getTotalIncome() == null) vo.setTotalIncome(BigDecimal.ZERO);
        if (vo.getTotalPatients() == null) vo.setTotalPatients(0L);
        if (vo.getTotalDoctors() == null) vo.setTotalDoctors(0L);
        if (vo.getTotalEvaluations() == null) vo.setTotalEvaluations(0L);

        return vo;
    }

    /**
     * 获取近 N 天趋势数据
     * @param days
     * @return
     */
    @Override
    @Cacheable(value = "dashboard:trend", key = "#days", unless = "#result == null")
    public List<TrendDataVO> getTrendData(int days) {

        log.info("查询趋势数据，天数：{}", days);

        // 限制最大查询天数，防止性能问题
        if (days > 90) {
            days = 90;
            log.warn("查询天数超过90，已自动限制为90");
        }

        List<TrendDataVO> list = dashboardMapper.selectTrendData(days, defaultPrice);

        // 补全缺失日期
        List<TrendDataVO> fullList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Map<LocalDate, TrendDataVO> dateMap = list.stream()
                .collect(Collectors.toMap(TrendDataVO::getDate, Function.identity()));

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            TrendDataVO vo = dateMap.getOrDefault(date, new TrendDataVO());
            vo.setDate(date);
            // 若存在则使用原有数据，否则使用默认值（0）
            if (!dateMap.containsKey(date)) {
                vo.setAppointments(0L);
                vo.setPaid(0L);
                vo.setIncome(BigDecimal.ZERO);
            }
            fullList.add(vo);
        }

        return fullList;

    }

    /**
     * 查询科室排行
     * @param limit
     * @return
     */
    @Override
    public List<DepartmentRankingVO> getDepartmentRanking(int limit) {
        log.info("查询科室排行，限制：{}", limit);

        if (limit <= 0) limit = 10;
        if (limit > 50) limit = 50; // 安全限制

        List<DepartmentRankingVO> list = dashboardMapper.selectDepartmentRanking(limit);

        // 处理百分比显示，保留两位小数
        list.forEach(vo -> {
            if (vo.getRatio() == null) vo.setRatio(BigDecimal.ZERO);
            vo.setRatio(vo.getRatio().setScale(2, BigDecimal.ROUND_HALF_UP));
        });

        return list;

    }

    /**
     * 获取医生排行
     * @param limit 前 N 名
     * @param sortBy 排序依据：'appointment' 或 'score'
     * @return
     */
    @Override
    public List<DoctorRankingVO> getDoctorRanking(int limit, String sortBy) {

        log.info("查询医生排行，限制：{}，排序：{}", limit, sortBy);

        if (limit <= 0) limit = 10;
        if (limit > 50) limit = 50;

        // 校验排序字段，防止 SQL 注入（已在 XML 中使用 <choose> 处理，但额外校验）
        if (!"score".equalsIgnoreCase(sortBy)) {
            sortBy = "appointment"; // 默认按挂号量
        }

        List<DoctorRankingVO> list = dashboardMapper.selectDoctorRanking(limit, sortBy);

        // 设置排名序号（从 1 开始）
        int rank = 1;
        for (DoctorRankingVO vo : list) {
            vo.setRank(rank++);
            if (vo.getAvgScore() == null) vo.setAvgScore(BigDecimal.ZERO);
        }

        return list;

    }

    /**
     * 获取预约状态分布
     * @return
     */
    @Override
    public List<StatusDistributionVO> getStatusDistribution() {

        log.info("查询状态分布");

        List<StatusDistributionVO> list = dashboardMapper.selectStatusDistribution();

        // 构建所有状态的默认集合
        Map<Integer, String> allStatuses = Map.of(
                0, "待支付", 1, "已支付", 2, "已就诊", 3, "已取消", 4, "已过期"
        );
        Map<Integer, StatusDistributionVO> resultMap = list.stream()
                .collect(Collectors.toMap(StatusDistributionVO::getStatus, Function.identity()));

        List<StatusDistributionVO> fullList = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : allStatuses.entrySet()) {
            StatusDistributionVO vo = resultMap.getOrDefault(entry.getKey(), new StatusDistributionVO());
            vo.setStatus(entry.getKey());
            vo.setLabel(entry.getValue());
            if (vo.getCount() == null) vo.setCount(0L);
            fullList.add(vo);
        }

        return fullList;

    }

}
