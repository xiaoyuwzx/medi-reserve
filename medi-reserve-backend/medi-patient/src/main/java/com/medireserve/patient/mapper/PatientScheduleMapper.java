package com.medireserve.patient.mapper;

import com.medireserve.common.dto.ScheduleCalendarVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者端 - 排班查询 Mapper
 * 注意：此 Mapper 放在患者端子包下，不在 common 中
 * 提供排班日历查询（只读操作）
 */
@Mapper
public interface PatientScheduleMapper {

    /**
     * 查询某医生在未来日期范围内的排班日历
     * @param doctorId
     * @param startDate
     * @param endDate
     * @return
     */
    List<ScheduleCalendarVO> findSchedulesByDoctorIdAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
