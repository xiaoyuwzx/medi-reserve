package com.medireserve.common.mapper;

import com.medireserve.common.dto.AppointmentListVO;
import com.medireserve.common.entity.Appointment;
import com.medireserve.common.entity.Schedule;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约挂号：挂号下单、支付、查询排班等
 */
@Mapper
public interface AppointmentMapper {

    /**
     * 根据排班ID查询排班信息
     * @param scheduleId
     * @return
     */
    @Select("select * from schedule where id = #{scheduleId}")
    Schedule findByScheduleId(@Param("scheduleId") Long scheduleId);

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
     * 根据排班ID扣除对应号源（数据库乐观锁）
     * 剩余号源>0时才能扣减；扣减后若为0则状态改为已满，否则保持原有状态
     */
    @Update("update schedule set " +
            "remaining_count = remaining_count - 1, " +
            "status = case " +
            "  when remaining_count - 1 = 0 then 3 " +
            "  else status " +
            "end " +
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
     * 查询超时未支付的预约（供启动扫描和时间轮双重校验）
     *
     * 说明：时间轮触发时，可能因为服务重启导致任务延迟，
     * 这里用数据库时间做最终判断，确保不会误取消
     *
     * @param id
     * @return
     */
    @Select("select * from appointment " +
            "where id = #{id} and status = 0 " +
            "and created_at < DATE_SUB(now(), interval 30 minute)")
    Appointment findPendingTimeout(@Param("id") Long id);

    /**
     * 更新预约状态（乐观锁版本）
     *
     * 核心：WHERE id = #{id} AND status = 0
     * - 只有当前状态是「待支付」时才能更新
     * - 返回 0 表示更新失败（状态已被其他操作改变）
     *
     * 为什么不用 CAS（Compare And Set）？
     * - 这是数据库级别的乐观锁，等价于 CAS
     * - 保证并发安全：支付和取消同时发生时，只有一个会成功
     *
     * @param id 预约ID
     * @param status 目标状态
     * @return 受影响行数
     */
    @Update("update appointment set status = #{status} where id = #{id} and status = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 回滚号源（取消预约时调用）
     * 剩余号源+1，如果当前状态为已满(3)则恢复为正常(1)
     *
     * 智能状态恢复逻辑：
     * - 如果排班当前是「已满（3）」状态，回滚号源后恢复为「正常（1）」
     * - 如果排班本来就是「正常（1）」或「停诊（2）」，保持原状态
     *
     * 为什么需要这个 CASE？
     * - 排班「已满」状态是系统自动设置的（remaining_count=0）
     * - 号源回滚后，剩余号源 > 0，应该自动恢复为「正常」
     * - 否则患者看到「已满」就不敢挂号了
     */
    @Update("update schedule set " +
            "remaining_count = remaining_count + 1, " +
            "status = case " +
            "  when status = 3 and remaining_count + 1 > 0 then 1 " +
            "  else status " +
            "end " +
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

    /**
     * 结束问诊：更新预约状态为已完成
     * 仅当状态为 1（已支付）时才能更新为 2（已完成）
     * @param id 预约ID
     * @return 受影响行数
     */
    @Update("update appointment set status = 2 where id = #{id} and status = 1")
    int finishConsultation(@Param("id") Long id);

    /**
     * 查询所有未来排班ID（今天及之后）
     * 用于初始化布隆过滤器
     */
    @Select("SELECT id FROM schedule WHERE schedule_date >= CURDATE()")
    List<Long> findFutureScheduleIds();

    /**
     * 分页查询我的预约列表（含关联医生、科室、职称、排班信息）
     */
    @Select("<script>" +
            "SELECT a.id, a.appointment_no as appointmentNo, a.schedule_id as scheduleId, " +
            "a.patient_id as patientId, a.doctor_id as doctorId, a.status, a.created_at as createdAt, " +
            "d.name as doctorName, dept.name as departmentName, t.name as titleName, " +
            "s.schedule_date as scheduleDate, s.period as period, " +
            "CASE WHEN s.period = 1 THEN '上午' ELSE '下午' END as periodText " +
            "FROM appointment a " +
            "LEFT JOIN doctor d ON a.doctor_id = d.id " +
            "LEFT JOIN department dept ON d.department_id = dept.id " +
            "LEFT JOIN title t ON d.title_id = t.id " +
            "LEFT JOIN schedule s ON a.schedule_id = s.id " +
            "WHERE a.patient_id = #{patientId} " +
            "<if test='status != null'> AND a.status = #{status} </if> " +
            "ORDER BY a.created_at DESC" +
            "</script>")
    List<AppointmentListVO> findMyAppointments(@Param("patientId") Long patientId,
                                                @Param("status") Integer status);

    /**
     * 统计我的预约总数
     */
    @Select("<script>" +
            "SELECT count(*) FROM appointment " +
            "WHERE patient_id = #{patientId} " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "</script>")
    int countMyAppointments(@Param("patientId") Long patientId,
                            @Param("status") Integer status);

    /**
     * 分页查询医生的预约列表（含关联患者信息）
     */
    @Select("<script>" +
            "SELECT a.id, a.appointment_no as appointmentNo, a.schedule_id as scheduleId, " +
            "a.patient_id as patientId, a.doctor_id as doctorId, a.status, a.created_at as createdAt, " +
            "d.name as doctorName, p.name as patientName, p.phone as patientPhone, " +
            "s.schedule_date as scheduleDate, s.period as period, " +
            "CASE WHEN s.period = 1 THEN '上午' ELSE '下午' END as periodText " +
            "FROM appointment a " +
            "LEFT JOIN doctor d ON a.doctor_id = d.id " +
            "LEFT JOIN patient p ON a.patient_id = p.id " +
            "LEFT JOIN schedule s ON a.schedule_id = s.id " +
            "WHERE a.doctor_id = #{doctorId} " +
            "<if test='date != null'> AND s.schedule_date = #{date} </if> " +
            "<if test='status != null'> AND a.status = #{status} </if> " +
            "ORDER BY a.created_at DESC" +
            "</script>")
    List<AppointmentListVO> findDoctorAppointments(@Param("doctorId") Long doctorId,
                                                    @Param("date") String date,
                                                    @Param("status") Integer status);

    /**
     * 统计医生的预约总数
     */
    @Select("<script>" +
            "SELECT count(*) FROM appointment a " +
            "LEFT JOIN schedule s ON a.schedule_id = s.id " +
            "WHERE a.doctor_id = #{doctorId} " +
            "<if test='date != null'> AND s.schedule_date = #{date} </if> " +
            "<if test='status != null'> AND a.status = #{status} </if>" +
            "</script>")
    int countDoctorAppointments(@Param("doctorId") Long doctorId,
                                @Param("date") String date,
                                @Param("status") Integer status);

}
