package com.medireserve.doctor.mapper;

import com.medireserve.common.entity.Schedule;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.ibatis.annotations.*;

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

    /**
     * 根据排班ID查询单条排班记录
     * @param id
     * @return
     */
    @Select("select * from schedule where id = #{id}")
    Schedule findById(Long id);

    /**
     * 更新排班状态：停诊/恢复
     * @param id
     * @param status
     */
    @Update("update schedule set status = #{status} where id = #{id}")
    void updateStatus(Long id, int status);

}
