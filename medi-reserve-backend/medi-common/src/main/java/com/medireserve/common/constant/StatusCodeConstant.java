package com.medireserve.common.constant;

/**
 * 操作状态码常量
 */
public class StatusCodeConstant {

    // ========== 通用状态码 ==========
    public static final Integer SUCCESS = 1;          // 操作成功
    public static final Integer ERROR = 0;            // 操作失败
    public static final Integer UNAUTHORIZED = 401;   // 未登录/Token无效
    public static final Integer FORBIDDEN = 403;      // 无权限
    public static final Integer NOT_FOUND = 404;      // 资源不存在
    public static final Integer SERVER_ERROR = 500;   // 服务器内部错误

    // ========== 业务状态码（1000+） ==========
    public static final Integer ACCOUNT_DISABLED = 1001;   // 账号被禁用
    public static final Integer ACCOUNT_NOT_EXIST = 1002;  // 账号不存在
    public static final Integer PASSWORD_ERROR = 1003;     // 密码错误
    public static final Integer PHONE_EXISTS = 1004;       // 手机号已注册
    public static final Integer USERNAME_EXISTS = 1005;    // 用户名已存在
    public static final Integer AUDIT_PENDING = 1006;      // 审核中
    public static final Integer AUDIT_REJECTED = 1007;     // 审核驳回
    public static final Integer PARAM_ERROR = 1008;     // 参数校验失败
    public static final Integer ACCOUNT_LOCKED = 1009;   // 账号被锁定

    // ========== 排班管理状态码 ==========
    public static final Integer SCHEDULE_DUPLICATE = 2001;        // 排班冲突（同一时段已有排班）
    public static final Integer SCHEDULE_NOT_FOUND = 2002;        // 排班不存在
    public static final Integer SCHEDULE_STATUS_INVALID = 2003;   // 目标状态不合法
    public static final Integer SCHEDULE_FULL = 2004;             // 排班号源已满，无法停诊
    public static final Integer SCHEDULE_HAS_APPOINTMENTS = 2005;      // 存在预约记录无法删除

    // ========== 医生审核状态码 ==========
    public static final Integer DOCTOR_NOT_FOUND = 3001;         // 医生不存在
    public static final Integer DOCTOR_ALREADY_AUDITED = 3002;   // 医生已审核
    public static final Integer DOCTOR_AUDIT_REJECT_REASON_EMPTY = 3003;  // 驳回原因不能为空
    public static final Integer DOCTOR_AUDIT_NOT_FOUND = 3004;                // 医生审核数据不存在
    public static final Integer AUDIT_OPERATION_FAILED = 3005;                // 审核操作失败

    // ========== 预约挂号状态码 ==========
    public static final Integer APPOINTMENT_DUPLICATE = 4001;      // 重复预约
    public static final Integer SCHEDULE_DATE_PAST = 4002;         // 排班日期已过
    public static final Integer APPOINTMENT_NOT_PENDING = 4003;    // 预约不是待支付状态
    public static final Integer APPOINTMENT_NOT_FOUND = 4004;      // 预约不存在
    public static final Integer APPOINTMENT_PAY_FAILED = 4005;     // 支付失败
    public static final Integer APPOINTMENT_ALREADY_PAID = 4009;       // 预约已支付（幂等场景）
    public static final Integer APPOINTMENT_TIMEOUT = 4010;            // 预约已超时

    // ========== 排班状态异常 ==========
    public static final Integer SCHEDULE_STOPPED = 4006;               // 排班已停诊
    public static final Integer SCHEDULE_ALREADY_FULL = 4007;          // 排班号源已满（挂号场景）
    public static final Integer INSUFFICIENT_QUOTA = 4008;             // 号源不足

    // ========== 系统级异常 ==========
    public static final Integer SYSTEM_BUSY = 5001;                    // 系统繁忙
    public static final Integer SYSTEM_ERROR = 5002;                   // 系统异常
    public static final Integer DEPARTMENT_NOT_FOUND = 5003;
    public static final Integer TITLE_NOT_FOUND = 5004;

    // ========== 评价相关状态码（6000+） ==========
    public static final Integer APPOINTMENT_NOT_EVALUABLE = 6001;      // 预约尚未就诊
    public static final Integer SCHEDULE_INFO_NOT_FOUND = 6002;        // 排班信息不存在
    public static final Integer SCHEDULE_DATE_NOT_ARRIVED = 6003;      // 就诊日期未到
    public static final Integer EVALUATION_DUPLICATE = 6004;           // 重复评价
    public static final Integer EVALUATION_NOT_FOUND = 6005;           // 评价不存在
    public static final Integer EVALUATION_ALREADY_DELETED = 6006;     // 评价已删除
    public static final Integer EVALUATION_DELETE_FAILED = 6007;       // 删除失败
    public static final Integer EVALUATION_CREATE_FAILED = 6008;       // 创建失败
    // ========== 缓存相关状态码 ==========
    public static final Integer CACHE_REFRESH_FAILED = 6009;   // 缓存刷新失败

}