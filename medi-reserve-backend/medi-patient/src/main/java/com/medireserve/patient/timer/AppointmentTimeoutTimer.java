package com.medireserve.patient.timer;

import com.medireserve.common.entity.Appointment;
import com.medireserve.common.mapper.AppointmentMapper;
import com.medireserve.patient.service.AppointmentTimeoutService;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AppointmentTimeoutTimer implements DisposableBean {

    // 创建一个时间轮：tickDuration=100ms，ticksPerWheel=512
    // 精度100ms，内存占用小，适合大量延迟任务
    private final HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    @Autowired
    private AppointmentMapper appointmentMapper;

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

    /**
     * 安排一个取消任务
     * @param appointmentId 预约ID
     * @param delay 延迟时间（30分钟）
     * @param unit 时间单位
     */
    public void scheduleCancel(Long appointmentId, long delay, TimeUnit unit) {
        // 将任务提交给时间轮，到达延迟时间后执行 CancelTask
        timer.newTimeout(new CancelTask(appointmentId), delay, unit);
        log.debug("已安排超时取消任务，预约ID：{}，延迟：{} {}", appointmentId, delay, unit);
    }

    // 内部任务类：调用独立的 Service，实现 TimerTask 接口
    private class CancelTask implements TimerTask {

        private final Long appointmentId;

        public CancelTask(Long appointmentId) {
            this.appointmentId = appointmentId;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            log.info("超时取消任务触发，预约ID：{}", appointmentId);
            // 直接调用 Service 的带锁方法（避免并发取消）
            timeoutService.cancelWithLock(appointmentId);
        }
    }

    @Override
    public void destroy() throws Exception {
        // 应用关闭时停止时间轮
        timer.stop();
        log.info("时间轮已关闭");
    }
}