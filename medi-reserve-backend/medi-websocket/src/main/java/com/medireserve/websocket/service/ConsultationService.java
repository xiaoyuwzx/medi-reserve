package com.medireserve.websocket.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.ChatMessageVO;
import com.medireserve.common.dto.ConsultationRoomVO;
import com.medireserve.common.entity.Appointment;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.Patient;
import com.medireserve.common.entity.Schedule;
import com.medireserve.common.exception.ConsultationException;
import com.medireserve.common.exception.PermissionDeniedException;
import com.medireserve.common.mapper.AppointmentMapper;
import com.medireserve.common.mapper.DoctorAuthMapper;
import com.medireserve.common.mapper.PatientAuthMapper;
import com.medireserve.websocket.mapper.ConsultationMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 问诊核心业务服务
 * 处理权限校验、房间信息、历史记录等
 */
@Slf4j
@Service
public class ConsultationService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private DoctorAuthMapper doctorAuthMapper;

    @Autowired
    private PatientAuthMapper patientAuthMapper;

    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    @Autowired
    private ConsultationRedisService consultationRedisService;

    // ==================== 权限校验 ====================

    /**
     * 校验用户是否有权进入该问诊室
     * @param appointmentId 预约ID
     * @param userId 当前用户ID
     * @param role 当前用户角色
     * @return 预约信息（便于后续使用）
     */
    public Appointment checkConsultationAccess(Long appointmentId, Long userId, String role) {
        // 1. 查询预约
        Appointment appointment = appointmentMapper.findById(appointmentId);
        if (appointment == null) {
            throw new ConsultationException("预约不存在");
        }

        // 2. 校验归属（患者或医生）
        boolean isPatient = "PATIENT".equals(role) && appointment.getPatientId().equals(userId);
        boolean isDoctor = "DOCTOR".equals(role) && appointment.getDoctorId().equals(userId);
        if (!isPatient && !isDoctor) {
            throw new PermissionDeniedException("您无权进入该问诊室");
        }

        // 3. 校验预约状态（必须已支付或已完成）
        if (!StatusConstant.APPOINTMENT_PAID.equals(appointment.getStatus())
                && !StatusConstant.APPOINTMENT_COMPLETED.equals(appointment.getStatus())) {
            throw new ConsultationException("预约尚未支付或已取消，无法进入问诊室");
        }

        // 4. 校验排班日期（必须是今天）
        Schedule schedule = appointmentMapper.findByScheduleId(appointment.getScheduleId());
        if (schedule == null) {
            throw new ConsultationException("排班信息不存在");
        }
        if (!schedule.getScheduleDate().equals(LocalDate.now())) {
            throw new ConsultationException("问诊通道仅在就诊当日（" + schedule.getScheduleDate() + "）开放");
        }

        // 5. 检查是否已结束（如有结束标记，可在 Redis 或数据库扩展，此处暂不强制）
        return appointment;
    }

    // ==================== 房间信息 ====================

    /**
     * 获取问诊室详细信息
     */
    public ConsultationRoomVO getRoomInfo(Long appointmentId, Long userId, String role) {
        // 先校验权限
        Appointment appointment = checkConsultationAccess(appointmentId, userId, role);

        // 查询医生信息
        Doctor doctor = doctorAuthMapper.findById(appointment.getDoctorId());
        // 查询患者信息
        Patient patient = patientAuthMapper.findById(appointment.getPatientId());
        // 查询排班日期
        Schedule schedule = appointmentMapper.findByScheduleId(appointment.getScheduleId());

        // 查询在线人数
        int onlineCount = consultationRedisService.getRoomOnlineCount(appointmentId);

        return ConsultationRoomVO.builder()
                .appointmentId(appointmentId)
                .patientId(patient.getId())
                .patientName(patient.getName())
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .departmentName(doctor.getDepartmentName())
                .scheduleDate(schedule.getScheduleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .status(1) // 默认进行中
                .statusText("进行中")
                .onlineCount(onlineCount)
                .build();
    }

    // ==================== 历史记录 ====================

    /**
     * 分页查询聊天历史记录
     */
    public PageInfo<ChatMessageVO> getHistory(Long appointmentId, Long userId, String role, int page, int size) {
        // 校验权限
        checkConsultationAccess(appointmentId, userId, role);

        PageHelper.startPage(page, size);
        List<ChatMessageVO> list = consultationMessageMapper.findByAppointmentId(appointmentId, userId);
        return new PageInfo<>(list);
    }

    // ==================== 结束问诊 ====================

    /**
     * 结束问诊（仅更新状态，实际业务中可扩展）
     */
    public void endConsultation(Long appointmentId, Long userId, String role) {
        // 校验权限
        checkConsultationAccess(appointmentId, userId, role);

        // 更新预约状态为已完成（2）
        int rows = appointmentMapper.updateStatus(appointmentId, StatusConstant.APPOINTMENT_COMPLETED);
        if (rows == 0) {
            throw new ConsultationException("结束问诊失败，请稍后重试");
        }

        // 清理 Redis 房间成员
        consultationRedisService.leaveRoom(appointmentId, userId);
        log.info("问诊结束，预约ID：{}，操作人：{}", appointmentId, userId);
    }
}