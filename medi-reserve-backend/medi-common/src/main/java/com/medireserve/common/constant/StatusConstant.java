package com.medireserve.common.constant;

/**
 * 业务状态常量（账号状态、审核状态等）
 */
public class StatusConstant {

    // ========== 通用账号状态（patient.status / doctor.status / admin.status） ==========
    public static final Integer ACCOUNT_NORMAL = 1;   // 正常
    public static final Integer ACCOUNT_DISABLED = 0; // 禁用

    // ========== 医生审核状态（doctor_audit.audit_status） ==========
    public static final Integer AUDIT_PENDING = 0;    // 待审核
    public static final Integer AUDIT_APPROVED = 1;   // 审核通过
    public static final Integer AUDIT_REJECTED = 2;   // 审核驳回

    // ========== 排班状态（schedule.status） ==========
    public static final Integer SCHEDULE_NORMAL = 1;  // 正常
    public static final Integer SCHEDULE_STOPPED = 2; // 已停诊
    public static final Integer SCHEDULE_FULL = 3;    // 已满

    // ========== 预约状态（appointment.status） ==========
    public static final Integer APPOINTMENT_PENDING = 0;     // 待支付
    public static final Integer APPOINTMENT_PAID = 1;        // 已支付（待就诊）
    public static final Integer APPOINTMENT_COMPLETED = 2;   // 已就诊
    public static final Integer APPOINTMENT_CANCELLED = 3;   // 已取消
    public static final Integer APPOINTMENT_EXPIRED = 4;     // 已过期

    // ========== 性别（patient.gender / doctor.gender） ==========
    public static final Integer GENDER_UNKNOWN = 0;   // 未知
    public static final Integer GENDER_MALE = 1;      // 男
    public static final Integer GENDER_FEMALE = 2;    // 女

    // ========== 时段（schedule.period / appointment.period） ==========
    public static final Integer PERIOD_MORNING = 1;   // 上午
    public static final Integer PERIOD_AFTERNOON = 2; // 下午

}