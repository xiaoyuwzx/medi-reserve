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

/**
 * 预约超时取消调度器（基于 Netty 时间轮）
 *
 * 核心职责：
 * 1. 为每个新创建的预约安排一个 30 分钟后的取消任务
 * 2. 服务启动时扫描数据库，补偿执行遗漏的超时任务
 * 3. 应用关闭时优雅停止时间轮
 *
 * 为什么不用 ScheduledExecutorService？
 * - ScheduledExecutorService 使用优先级队列，插入和删除是 O(log n)
 * - HashedWheelTimer 是 O(1) 插入，内存效率更高，适合海量延迟任务
 */
@Slf4j
@Component
public class AppointmentTimeoutTimer implements DisposableBean {

    /**
     * Netty 时间轮核心组件
     *
     * 参数说明：
     * - tickDuration: 100ms → 时间轮每 100ms 走一格（精度 100ms）
     * - TimeUnit.MILLISECONDS: 时间单位
     * - ticksPerWheel: 512 → 时间轮有 512 个槽位（环形数组大小）
     *
     * 性能估算：
     * - 内存占用：512 个槽位 × 每个槽位持有任务链表
     * - 每个任务占用约 100-200 字节
     * - 10 万个等待任务约占用 20MB 内存（内存占用小，适合大量延迟任务）
     */
    private final HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private AppointmentTimeoutService timeoutService;  // 注入独立的 Service

    // 启动补偿扫描（防止服务重启导致任务丢失）
    // 启动扫描：增加分布式锁 + 失败统计 + 缓存清除
    /**
     * 服务启动时自动执行（@PostConstruct）
     *
     * 问题场景：
     * 1. 预约创建后，时间轮里安排了一个 30 分钟后的任务
     * 2. 但 5 分钟后，服务重启了 → JVM 内存里的时间轮任务全部丢失
     * 3. 那些预约永远没人取消了 → 号源被永久占用
     *
     * 解决方案：
     * - 启动时扫描数据库，找出所有「待支付且创建时间 > 30 分钟」的预约
     * - 逐个调用取消逻辑（带分布式锁，防止多实例重复执行）
     *
     * 为什么这个方案能覆盖所有遗漏场景？
     * - 无论服务重启几次，启动时都会执行一次扫描
     * - 只要数据库里的超时预约存在，就一定会被处理
     */
    @PostConstruct
    public void init() {
        log.info("启动时扫描超时未支付的预约...");
        // 查询所有超时预约：status=0 且 created_at < NOW() - 30min
        List<Appointment> timeoutList = appointmentMapper.findAllPendingTimeout();
        int successCount = 0;
        int failCount = 0;

        for (Appointment appointment : timeoutList) {
            try {
                // 使用带锁的取消方法（防止多实例并发）
                // Service 层包含事务、号源回滚、缓存清除
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

    // 安排延迟任务
    /**
     * 为指定预约安排一个超时取消任务
     *
     * 调用时机：预约创建成功后（AppointmentServiceImpl.createAppointment）
     * 延迟时间：30 分钟（由调用方传入）
     *
     * 执行原理：
     * 1. timer.newTimeout() 将任务提交到时间轮
     * 2. 时间轮根据当前时间 + 延迟时间，计算目标槽位
     * 3. 到达目标时间时，由时间轮线程池执行 CancelTask.run()
     *
     * @param appointmentId 预约ID
     * @param delay         延迟时间（30 分钟）
     * @param unit          时间单位（分钟）
     */
    public void scheduleCancel(Long appointmentId, long delay, TimeUnit unit) {

        // 将任务提交给时间轮，到达延迟时间后执行 CancelTask
        timer.newTimeout(new CancelTask(appointmentId), delay, unit);

        log.debug("已安排超时取消任务，预约ID：{}，延迟：{} {}", appointmentId, delay, unit);

    }

    // 内部任务类：调用独立的 Service，实现 TimerTask 接口
    /**
     * 取消任务（由时间轮线程执行）
     *
     * 注意：
     * 1. 时间轮线程池是单线程的（避免并发问题）
     * 2. run() 方法不能抛出异常，否则任务会停止
     * 3. 实际业务逻辑委托给 Service，保证事务和锁的正确使用
     */
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

    /**
     * 应用关闭时，停止时间轮（释放资源）
     *
     * 注意：调用 timer.stop() 后：
     * 1. 不再接受新任务
     * 2. 等待所有正在执行的任务完成（最多等待 1 秒）
     * 3. 如果任务执行时间过长，会被强制中断
     */
    @Override
    public void destroy() throws Exception {
        // 应用关闭时停止时间轮
        timer.stop();
        log.info("时间轮已关闭");
    }
}