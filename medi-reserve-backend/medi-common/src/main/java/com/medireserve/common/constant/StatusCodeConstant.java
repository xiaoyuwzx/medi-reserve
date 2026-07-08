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

}