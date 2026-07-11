package com.medireserve.patient.mapper;

import com.medireserve.common.entity.Appointment;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.Schedule;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 预约挂号：挂号下单、支付、查询排班等
 */
@Mapper
public interface AppointmentMapper {

    /**
     * 根据排班ID查询排班信息
     * @param id
     * @return
     */
    @Select("select * from schedule where id = #{id}")
    Schedule findByScheduleId(@Param("id") Long id);

    /**
     * 根据患者ID检查是否已经预约了该排班
     * @param patientId
     * @param scheduleId
     * @return
     */
    @Select("select count(*) from appointment " +
            "where schedule_id = #{scheduleId} and patient_id = #{patientId} and status in (0, 1)")
    int countByPatientAndSchedule(@Param("patientId") Long patientId, @Param("scheduleId") Long scheduleId);

    /**
     * 根据排班ID扣除对应号源
     * @param scheduleId
     * @return
     */
    @Update("update schedule set " +
            "remaining_count = remaining_count - 1, " +
            "status = case when remaining_count - 1 = 0 then 3 else 1 end " +
            "where id = #{scheduleId} and remaining_count > 0")
    int decrementRemainingCount(@Param("scheduleId") Long scheduleId);

    /**
     * 插入预约记录
     * @param appointment
     * @return
     */
    @Insert("insert into appointment (appointment_no, schedule_id, patient_id, doctor_id, status) " +
            "values (#{appointmentNo}, #{scheduleId}, #{patientId}, #{doctorId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Appointment appointment);

    /**
     * 查询超时未支付的预约
     * @param id
     * @return
     */
    @Select("select * from appointment " +
            "where id = #{id} and status = 0 " +
            "and created_at < DATE_SUB(now(), interval 30 minute)")
    Appointment findPendingTimeout(@Param("id") Long id);

    /**
     * 更新预约状态(支付成功/取消/过期)
     * @param id
     * @param status
     * @return
     */
    @Update("update appointment set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 回滚号源
     * @param scheduleId
     * @return
     */
    @Update("update schedule set " +
            "remaining_count = remaining_count + 1, " +
            "status = 1 " +
            "where id = #{scheduleId}")
    int incrementRemainingCount(@Param("scheduleId") Long scheduleId);

    /**
     * 根据预约ID查询预约
     * @param id
     * @return
     */
    @Select("select * from appointment where id = #{id}")
    Appointment findById(@Param("id") Long id);

    /**
     * 查询所有超时未支付的预约（服务启动扫描用）
     * @return 超时预约列表
     */
    @Select("select * from appointment where status = 0 and created_at < date_sub(now(), interval 30 minute)")
    List<Appointment> findAllPendingTimeout();
}
