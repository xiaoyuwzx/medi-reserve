package com.medireserve.patient.service.impl;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.AppointmentCreateDTO;
import com.medireserve.common.dto.ScheduleDetailVO;
import com.medireserve.common.entity.Appointment;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.Schedule;
import com.medireserve.common.exception.*;
import com.medireserve.patient.mapper.AppointmentMapper;
import com.medireserve.patient.service.AppointmentService;
import com.medireserve.patient.timer.AppointmentTimeoutTimer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 预约挂号：挂号下单、支付、查询排班等
 */
@Slf4j
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RedissonClient redissonClient;// 分布式锁客户端

    @Autowired
    private AppointmentTimeoutTimer timeoutTimer;// 时间轮工具

    /**
     * 查询排班详细
     * @param scheduleId
     * @return
     */
    @Override
    public ScheduleDetailVO getScheduleDetail(Long scheduleId) {

        log.info("查询排班详细，排班ID：{}", scheduleId);

        //查询排班
        Schedule schedule = appointmentMapper.findByScheduleId(scheduleId);
        if(schedule == null){
            log.warn("排班不存在，排班ID：{}", scheduleId);
            throw new ScheduleNotFoundException();
        }

        //查询医生信息
        Doctor doctor = appointmentMapper.findByDoctorId(schedule.getDoctorId());
        if(doctor == null){
            log.warn("医生不存在，医生ID：{}", schedule.getDoctorId());
            throw new DoctorNotFoundException();
        }

        //组装返回对象
        ScheduleDetailVO scheduleDetailVO = new ScheduleDetailVO();
        scheduleDetailVO.setScheduleId(schedule.getId());
        scheduleDetailVO.setDoctorId(doctor.getId());
        scheduleDetailVO.setDoctorName(doctor.getName());
        scheduleDetailVO.setDepartment(doctor.getDepartment());
        scheduleDetailVO.setTitle(doctor.getTitle());
        scheduleDetailVO.setScheduleDate(schedule.getScheduleDate());
        scheduleDetailVO.setPeriod(schedule.getPeriod());
        scheduleDetailVO.setPeriodText(schedule.getPeriod() == 1 ? "上午" : "下午");
        scheduleDetailVO.setRemainingCount(schedule.getRemainingCount());
        scheduleDetailVO.setStatus(schedule.getStatus());
        //文本状态
        String statusText;
        if(schedule.getStatus() == 1){
            statusText = "正常";
        }else if(schedule.getStatus() == 2){
            statusText = "已停诊";
        }else if(schedule.getStatus() == 3){
            statusText = "已满";
        }else{
            statusText = "未知";
        }
        scheduleDetailVO.setStatusText(statusText);

        log.info("排班详细查询成功，排班ID：{}", scheduleId);

        return scheduleDetailVO;

    }

    /**
     * 创建预约(下单)
     * @param patientId
     * @param appointmentCreateDTO
     * @return
     */
    @Override
    @Transactional//事务控制
    public Appointment createAppointment(Long patientId, AppointmentCreateDTO appointmentCreateDTO) {

        Long scheduleId = appointmentCreateDTO.getScheduleId();

        log.info("创建预约，患者ID：{}，排班ID：{}", patientId, scheduleId);

        //校验排班是否存在
        Schedule schedule = appointmentMapper.findByScheduleId(scheduleId);
        if(schedule == null){
            log.warn("排班不存在，排班ID：{}", scheduleId);
            throw new ScheduleNotFoundException();
        }

        //校验排班日期是否已过时
        if(schedule.getScheduleDate().isBefore(LocalDate.now())){
            log.warn("排班日期已过时，排班ID：{}，日期：{}", scheduleId, schedule.getScheduleDate());
            throw new ScheduleDatePastException();
        }

        //校验排班状态
        if(StatusConstant.SCHEDULE_STOPPED.equals(schedule.getStatus())){
            log.warn("排班已停诊，排班ID：{}", scheduleId);
            throw new ScheduleStoppedException();
        }
        if(StatusConstant.SCHEDULE_FULL.equals(schedule.getStatus())){
            log.warn("排班号源已满，排班ID：{}", scheduleId);
            throw new ScheduleAlreadyFullException();
        }

        //校验是否重复预约
        int existCount = appointmentMapper.countByPatientAndSchedule(patientId, scheduleId);
        if(existCount > 0){
            log.warn("重复预约，患者ID：{}，排班ID：{}", patientId, scheduleId);
            throw new AppointmentDuplicateException();
        }

        //分布式锁扣除号源
        String lockKey = "lock:schedule:" + scheduleId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {

            //尝试获取锁，等待3秒，锁持有时间最多10秒
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if(!locked){
                log.error("获取分布式锁失败，排班ID：{}", scheduleId);
                throw new SystemBusyException();
            }

            //扣减号源(数据库乐观锁)
            int rows = appointmentMapper.decrementRemainingCount(scheduleId);
            if(rows == 0){
                log.warn("扣减号源失败，号源不足，排班ID：{}", scheduleId);
                throw new InsufficientQuotaException();
            }

            //重新查询排班
            schedule = appointmentMapper.findByScheduleId(scheduleId);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断：", e);
            throw new SystemException();

        } finally {

            //释放锁
            if(locked && lock.isHeldByCurrentThread()){
                lock.unlock();
                log.debug("释放分布式锁成功，排班ID：{}", scheduleId);
            }

        }

        //插入预约记录
        Appointment appointment = new Appointment();
        //生成预约单号：APPOINTMENT_ + 时间戳 + 随机四位数
        String appointmentNo = "APPOINTMENT_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                                    + String.format("%04d", (int)(Math.random() * 10000));
        appointment.setAppointmentNo(appointmentNo);
        appointment.setScheduleId(scheduleId);
        appointment.setPatientId(patientId);
        appointment.setDoctorId(schedule.getDoctorId());
        appointment.setStatus(StatusConstant.APPOINTMENT_PENDING);

        //向数据库插入数据
        appointmentMapper.insert(appointment);

        log.info("预约记录插入成功，预约ID：{}，预约单号：{}", appointment.getId(), appointment.getAppointmentNo());

        //启动超时取消倒计时
        //30分钟后检查该预约是否仍为待支付，如果是则取消并回滚号源
        timeoutTimer.scheduleCancel(appointment.getId(), 30, TimeUnit.MINUTES);

        log.info("已启动超时取消倒计时，预约ID：{}，30分钟后执行", appointment.getId());

        return appointment;

    }

    /**
     * 模拟支付
     * @param appointmentId
     * @param patientId
     */
    @Override
    @Transactional//事务控制
    public void payAppointment(Long appointmentId, Long patientId) {

        log.info("模拟支付，预约ID：{}，患者ID：{}",appointmentId, patientId);

        //校验预约是否存在
        Appointment appointment = appointmentMapper.findById(appointmentId);
        if(appointment == null){
            log.warn("预约不存在，预约ID：{}", appointmentId);
            throw new AppointmentNotFoundException();
        }

        //校验归属(防止越权支付)
        if(!appointment.getPatientId().equals(patientId)){
            log.warn("支付越权，预约ID：{}，当前患者ID：{}，预约归属患者ID：{}", appointmentId,patientId, appointment.getPatientId());
            throw new PermissionDeniedException("您无权支付该预约");
        }

        //判断是否已支付预约
        if(StatusConstant.APPOINTMENT_PAID.equals(appointment.getStatus())){
            log.info("预约已支付，无需重复操作，预约ID：{}", appointmentId);
            throw new AppointmentAlreadyPaidException();
        }

        //校验预约是否超时
        LocalDateTime deadline = appointment.getCreatedAt().plusMinutes(30);
        if (LocalDateTime.now().isAfter(deadline)) {
            log.warn("预约已超时，无法支付，预约ID：{}，创建时间：{}，截止时间：{}",
                    appointmentId, appointment.getCreatedAt(), deadline);
            throw new AppointmentTimeoutException();
        }

        //判断预约状态是否是待支付(0)
        if(!StatusConstant.APPOINTMENT_PENDING.equals(appointment.getStatus())){
            log.warn("预约状态不是待支付，当前状态：{}，预约ID：{}", appointment.getStatus(), appointmentId);
            throw new AppointmentNotPendingException();
        }

        //更新状态为已支付(1)
        int rows = appointmentMapper.updateStatus(appointmentId, StatusConstant.APPOINTMENT_PAID);
        if(rows == 0){
            log.error("支付失败，更新数据库无影响，预约ID：{}", appointmentId);
            throw new PaymentFailedException();
        }

        log.info("支付成功，预约ID：{}", appointmentId);

    }

}
