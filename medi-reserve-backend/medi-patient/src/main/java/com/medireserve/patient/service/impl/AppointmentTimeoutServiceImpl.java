package com.medireserve.patient.service.impl;

import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.entity.Appointment;
import com.medireserve.common.entity.Schedule;
import com.medireserve.common.mapper.AppointmentMapper;
import com.medireserve.patient.service.AppointmentTimeoutService;
import com.medireserve.patient.service.PatientDoctorService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AppointmentTimeoutServiceImpl implements AppointmentTimeoutService {

    /**
     * 自注入当前 Service 的代理对象（通过 @Lazy 避免循环依赖），解决 @Transactional 失效问题
     * 原因：Spring 的 @Transactional 通过代理生效，若在 cancelWithLock() 中
     * 直接调用 this.cancelExpiredAppointment()，事务注解会失效（因为没走代理）。
     * 通过 self.cancelExpiredAppointment() 调用，确保走代理链路，事务正常开启。
     */
    @Autowired
    @Lazy
    private AppointmentTimeoutService self;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private PatientDoctorService patientDoctorService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 带分布式锁的取消方法（供时间轮和启动扫描调用）
     */
    @Override
    public void cancelWithLock(Long appointmentId) {
        String lockKey = "lock:cancel:" + appointmentId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 等待3秒，持有10秒（取消操作很快，10秒足够）
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                // 调用事务方法（通过自注入代理）
                self.cancelExpiredAppointment(appointmentId);
            } else {
                log.warn("获取取消任务锁失败，预约ID：{}，可能已被其他节点处理", appointmentId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取取消锁被中断，预约ID：{}", appointmentId, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 实际取消逻辑
     * 为什么单独拆出来？因为要让事务生效，必须由 Spring 代理调用。
     * 取消超时预约（带分布式锁 + 事务）
     * @param appointmentId 预约ID
     * @return 是否成功取消
     */
    @Override
    @Transactional
    public boolean cancelExpiredAppointment(Long appointmentId) {
        log.info("取消超时预约，预约ID：{}", appointmentId);

        // 查询预约，必须仍为待支付且已超过30分钟
        Appointment appointment = appointmentMapper.findPendingTimeout(appointmentId);
        if (appointment == null) {
            log.info("预约无需取消，预约ID：{}（可能已支付或已取消）", appointmentId);
            return false;
        }

        // 更新预约状态为 3-已取消（乐观锁条件：status=0）
        int rows = appointmentMapper.updateStatus(appointmentId, StatusConstant.APPOINTMENT_CANCELLED);
        if (rows == 0) {
            // 更新失败（可能被支付抢先了）
            log.error("取消预约失败，更新数据库无影响，预约ID：{}", appointmentId);
            return false;
        }

        // 回滚号源（remaining_count + 1）
        Long scheduleId = appointment.getScheduleId();
        appointmentMapper.incrementRemainingCount(scheduleId);

        // 清除该医生的排班缓存（让剩余号源对外可见）
        Schedule schedule = appointmentMapper.findByScheduleId(scheduleId);
        if (schedule != null) {
            patientDoctorService.clearScheduleCache(schedule.getDoctorId());
            log.info("已清除医生排班缓存，医生ID：{}", schedule.getDoctorId());
        }

        log.info("超时取消成功，预约ID：{}，排班ID：{}，号源已回滚", appointmentId, scheduleId);
        return true;
    }
}