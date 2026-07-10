package com.medireserve.patient.timer;

import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.entity.Appointment;

import com.medireserve.patient.mapper.AppointmentMapper;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 预约超时取消时间轮
 * 使用 Netty HashedWheelTimer 实现延迟任务，替代 @Scheduled 扫表
 * 优点：O(1) 触发，精准到秒，减少数据库压力
 */
@Slf4j
@Component
public class AppointmentTimeoutTimer implements DisposableBean {

    private final HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 为指定预约安排超时取消任务
     * @param appointmentId 预约ID
     * @param delay 延迟时间
     * @param unit 时间单位
     */
    public void scheduleCancel(Long appointmentId, long delay, TimeUnit unit) {
        timer.newTimeout(new CancelTask(appointmentId), delay, unit);
        log.debug("已安排超时取消任务，预约ID：{}，延迟：{} {}", appointmentId, delay, unit);
    }

    /**
     * 超时取消任务
     */
    private class CancelTask implements TimerTask {

        private final Long appointmentId;

        public CancelTask(Long appointmentId) {
            this.appointmentId = appointmentId;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            log.info("超时取消任务触发，预约ID：{}", appointmentId);

            // 1. 分布式锁（防止集群环境下多节点同时执行）
            String lockKey = "lock:cancel:" + appointmentId;
            RLock lock = redissonClient.getLock(lockKey);
            boolean locked = false;
            try {
                locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
                if (!locked) {
                    log.warn("获取取消任务锁失败，预约ID：{}，可能已被其他节点处理", appointmentId);
                    return;
                }

                // 2. 执行取消逻辑
                cancelExpiredAppointment(appointmentId);

            } catch (Exception e) {
                log.error("取消任务执行异常，预约ID：{}", appointmentId, e);
            } finally {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        /**
         * 执行取消操作（事务）
         */
        @Transactional
        public void cancelExpiredAppointment(Long appointmentId) {
            // 2.1 查询预约，必须仍为待支付且已超过30分钟
            Appointment appointment = appointmentMapper.findPendingTimeout(appointmentId);
            if (appointment == null) {
                // 已被支付或已取消，或未超时
                log.info("预约无需取消，预约ID：{}（可能已支付或已取消）", appointmentId);
                return;
            }

            // 2.2 更新预约状态为已取消（3）
            int rows = appointmentMapper.updateStatus(appointmentId, StatusConstant.APPOINTMENT_CANCELLED);
            if (rows == 0) {
                log.error("取消预约失败，更新数据库无影响，预约ID：{}", appointmentId);
                return;
            }

            // 2.3 回滚号源（remaining_count + 1）
            Long scheduleId = appointment.getScheduleId();
            appointmentMapper.incrementRemainingCount(scheduleId);

            log.info("超时取消成功，预约ID：{}，排班ID：{}，号源已回滚", appointmentId, scheduleId);
        }
    }

    /**
     * 关闭时间轮，释放资源
     */
    @Override
    public void destroy() throws Exception {
        timer.stop();
        log.info("时间轮已关闭");
    }
}