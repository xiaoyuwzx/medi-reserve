package com.medireserve.patient.timer;

import com.medireserve.common.entity.Appointment;
import com.medireserve.common.mapper.AppointmentMapper;
import com.medireserve.patient.service.AppointmentTimeoutService;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AppointmentTimeoutTimer implements DisposableBean {

    private final HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AppointmentTimeoutService timeoutService;  // 注入独立的 Service

    // 启动扫描：增加分布式锁 + 失败统计 + 缓存清除
    @PostConstruct
    public void init() {
        log.info("启动时扫描超时未支付的预约...");
        List<Appointment> timeoutList = appointmentMapper.findAllPendingTimeout();
        int successCount = 0;
        int failCount = 0;

        for (Appointment appointment : timeoutList) {
            try {
                // 使用带锁的取消方法（Service 层包含事务和缓存清除）
                timeoutService.cancelWithLock(appointment.getId());
                successCount++;
                log.info("启动时自动取消超时预约成功，预约ID：{}", appointment.getId());
            } catch (Exception e) {
                failCount++;
                log.error("启动时取消超时预约失败，预约ID：{}", appointment.getId(), e);
            }
        }
        log.info("扫描完成，成功取消 {} 个，失败 {} 个超时预约", successCount, failCount);
    }

    public void scheduleCancel(Long appointmentId, long delay, TimeUnit unit) {
        timer.newTimeout(new CancelTask(appointmentId), delay, unit);
        log.debug("已安排超时取消任务，预约ID：{}，延迟：{} {}", appointmentId, delay, unit);
    }

    // 内部任务类：调用独立的 Service
    private class CancelTask implements TimerTask {

        private final Long appointmentId;

        public CancelTask(Long appointmentId) {
            this.appointmentId = appointmentId;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            log.info("超时取消任务触发，预约ID：{}", appointmentId);
            // 直接调用 Service 的带锁方法
            timeoutService.cancelWithLock(appointmentId);
        }
    }

    @Override
    public void destroy() throws Exception {
        timer.stop();
        log.info("时间轮已关闭");
    }
}