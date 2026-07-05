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
}