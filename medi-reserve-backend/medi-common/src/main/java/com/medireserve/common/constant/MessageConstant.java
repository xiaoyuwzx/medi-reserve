package com.medireserve.common.constant;

/**
 * 提示信息常量
 */
public class MessageConstant {

    // ========== 通用 ==========
    public static final String SUCCESS = "操作成功";
    public static final String ERROR = "操作失败";
    public static final String UNKNOWN_ERROR = "未知错误，请稍后重试";
    public static final String USER_NOT_LOGIN = "用户未登录，请先登录";

    // ========== 登录注册 ==========
    public static final String LOGIN_SUCCESS = "登录成功";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_DISABLED = "账号已被禁用，请联系管理员";
    public static final String ACCOUNT_LOCKED = "账号被锁定，请稍后重试";
    public static final String PHONE_EXISTS = "手机号已被注册";
    public static final String USERNAME_EXISTS = "用户名已被占用";
    public static final String REGISTER_SUCCESS = "注册成功，请登录";
    public static final String REGISTER_FAILED = "注册失败";

    // ========== 令牌（JWT） ==========
    public static final String TOKEN_INVALID = "Token无效或已过期";
    public static final String TOKEN_MISSING = "未携带Token，请登录";
    public static final String TOKEN_VERIFY_FAILED = "Token验证失败";

    // ========== 权限 ==========
    public static final String PERMISSION_DENIED = "您没有权限执行此操作";
    public static final String PERMISSION_CREATE_ADMIN = "只有超级管理员才可以创建管理员账号";

    // ========== 参数校验（DTO中使用） ==========
    public static final String VALIDATION_ACCOUNT_NOT_EMPTY = "账号不能为空";
    public static final String VALIDATION_PASSWORD_NOT_EMPTY = "密码不能为空";
    public static final String VALIDATION_NAME_NOT_EMPTY = "姓名不能为空";
    public static final String VALIDATION_PHONE_NOT_EMPTY = "手机号不能为空";
    public static final String VALIDATION_DEPARTMENT_NOT_EMPTY = "科室不能为空";
    public static final String VALIDATION_TITLE_NOT_EMPTY = "职称不能为空";

    public static final String VALIDATION_PHONE_FORMAT = "手机号格式不正确";
    public static final String VALIDATION_PASSWORD_FORMAT = "密码需为6-20位字母和数字组合";
    public static final String VALIDATION_ID_CARD_FORMAT = "身份证号格式不正确";
    public static final String VALIDATION_EMAIL_FORMAT = "邮箱格式不正确";
    public static final String VALIDATION_USERNAME_FORMAT = "用户名需为4-20位字母数字下划线组合";

    // ========== 医生审核 ==========
    public static final String DOCTOR_REGISTER_PENDING = "注册成功，请等待管理员审核";
    public static final String DOCTOR_AUDIT_PENDING = "待审核";
    public static final String DOCTOR_AUDIT_APPROVED = "审核通过";
    public static final String DOCTOR_AUDIT_REJECTED = "审核驳回";
    public static final String AUDIT_PENDING_MSG = "账号正在审核中，请耐心等待管理员审核";
    public static final String AUDIT_REJECTED_MSG = "账号审核未通过，请查看管理员备注信息";

    // ========== 业务操作 ==========
    public static final String NOT_FOUND = "数据不存在";
    public static final String OPERATION_FAILED = "操作失败，请重试";
    public static final String AlREADY_EXISTS = "数据已存在";
}