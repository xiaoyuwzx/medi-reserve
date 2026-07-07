package com.medireserve.doctor.mapper;

import com.medireserve.common.entity.Schedule;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班管理业务接口
 */
@Mapper
public interface ScheduleMapper {

    /**
     * 校验唯一性：查询该医生在同一时间端内是否已有排班
     * @param doctorId
     * @param scheduleDate
     * @param period
     * @return
     */
    @Select("select count(*) from schedule " +
            "where doctor_id = #{doctorId} and schedule_date = #{scheduleDate} and period = #{period}")
    int countByDoctorDatePeriod(Long doctorId, LocalDate scheduleDate, Integer period);

    /**
     * 插入新排班
     * @param schedule
     * @return
     */
    @Insert("insert into schedule (doctor_id, schedule_date, period, max_count, remaining_count, status) " +
            "values (#{doctorId}, #{scheduleDate}, #{period}, #{maxCount}, #{remainingCount}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Schedule schedule);

    /**
     * 根据医生ID查询排班列表
     * @param doctorId
     * @param startDate
     * @param endDate
     * @return
     */
    List<Schedule> findByDoctorIdAndDateRange(Long doctorId, LocalDate startDate, LocalDate endDate);
    
}
