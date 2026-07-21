package com.medireserve.admin.mapper;

import com.medireserve.common.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端数据统计 Mapper 接口
 */
@Mapper
public interface AdminDashboardMapper {

    /**
     * 查询总览统计数据
     * @param defaultPrice 默认挂号费（用于估算收入）
     * @return 总览统计 VO
     */
    DashboardOverviewVO selectOverviewStatistics(@Param("defaultPrice") BigDecimal defaultPrice);

    /**
     * 查询近 N 天趋势数据
     * @param days         天数（含今天）
     * @param defaultPrice 默认挂号费
     * @return 按日期升序排列的趋势列表
     */
    List<TrendDataVO> selectTrendData(@Param("days") int days,
                                      @Param("defaultPrice") BigDecimal defaultPrice);

    /**
     * 查询科室挂号排行（按预约数降序）
     * @param limit 返回前 N 条
     * @return 科室排行列表
     */
    List<DepartmentRankingVO> selectDepartmentRanking(@Param("limit") int limit);

    /**
     * 查询医生挂号排行（可排序依据）
     * @param limit 返回前 N 条
     * @param sortBy 排序字段：'appointment' 或 'score'
     * @return 医生排行列表
     */
    List<DoctorRankingVO> selectDoctorRanking(@Param("limit") int limit, @Param("sortBy") String sortBy);

    /**
     * 查询预约状态分布
     * @return
     */
    List<StatusDistributionVO> selectStatusDistribution();

}
