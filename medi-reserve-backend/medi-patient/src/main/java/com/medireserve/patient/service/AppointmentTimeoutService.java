package com.medireserve.patient.service;

public interface AppointmentTimeoutService {

    /**
     * 取消超时预约（事务方法）
     * 供时间轮调用，保证事务生效
     */
    boolean cancelExpiredAppointment(Long appointmentId);

    /**
     * 带分布式锁的取消方法（供时间轮和启动扫描调用）
     */
    void cancelWithLock(Long appointmentId);

}
