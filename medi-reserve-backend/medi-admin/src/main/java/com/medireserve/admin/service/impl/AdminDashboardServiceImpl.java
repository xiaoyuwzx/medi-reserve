package com.medireserve.admin.service.impl;

import com.medireserve.admin.mapper.AdminDashboardMapper;
import com.medireserve.admin.service.AdminDashboardService;
import com.medireserve.common.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
    public List<TrendDataVO> getTrendData(int days) {

        log.info("查询趋势数据，天数：{}", days);

        // 限制最大查询天数，防止性能问题
        if (days > 90) {
            days = 90;
            log.warn("查询天数超过90，已自动限制为90");
        }

        List<TrendDataVO> list = dashboardMapper.selectTrendData(days, defaultPrice);

        // 补充缺失日期的数据（由于分组查询可能跳过无预约的日期，但前端通常需要连续日期）
        // 此处简单处理，如需连续日期可在 Java 中补全，但推荐前端处理。

        return list;

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

        // 确保所有状态都有标签，即使数据库没有数据
        // 但 Mapper 已通过 CASE WHEN 生成 label，所以无需额外处理

        return list;

    }

}
