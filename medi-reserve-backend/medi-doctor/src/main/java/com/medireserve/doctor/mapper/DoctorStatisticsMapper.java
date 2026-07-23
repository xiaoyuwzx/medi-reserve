package com.medireserve.doctor.mapper;

import com.medireserve.common.dto.DailyTrendVO;
import com.medireserve.common.dto.DoctorEvaluationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 医生端统计数据 Mapper
 * 只读查询，无写操作
 */
@Mapper
public interface DoctorStatisticsMapper {

    /**
     * 查询医生总接诊人数（已支付 + 已完成）
     */
    Long countTotalPatients(@Param("doctorId") Long doctorId);

    /**
     * 查询医生今日接诊人数（已支付 + 已完成）
     */
    Long countTodayPatients(@Param("doctorId") Long doctorId);

    /**
     * 查询医生的平均评分和评价总数
     * 仅统计已发布的评价（status = 1）
     */
    BigDecimal avgScore(@Param("doctorId") Long doctorId);

    /**
     * 查询医生的好评率（评分 >= 4 的比例）
     * 返回 0~100 的百分比数值
     */
    BigDecimal positiveRate(@Param("doctorId") Long doctorId);

    /**
     * 查询医生待处理问诊数（状态为已支付但尚未完成）
     */
    Long countPendingConsultations(@Param("doctorId") Long doctorId);

    /**
     * 查询医生近 N 天每日接诊量（按日期分组）
     * 若某天无数据，由 Service 补全
     */
    List<DailyTrendVO> selectDailyTrend(@Param("doctorId") Long doctorId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * 分页查询医生收到的评价列表（按时间倒序）
     * 返回评价信息及患者姓名（匿名处理）
     */
    List<DoctorEvaluationVO> selectEvaluations(@Param("doctorId") Long doctorId);

    /**
     * 统计评价总数
     */
    long countEvaluations(@Param("doctorId") Long doctorId);
}