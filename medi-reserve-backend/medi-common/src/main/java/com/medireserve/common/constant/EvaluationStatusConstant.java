package com.medireserve.common.constant;

/**
 * 评价状态常量
 * 对应 evaluation 表的 status 字段
 */
public class EvaluationStatusConstant {

    /** 待审核（暂未使用，预留） */
    public static final Integer PENDING = 0;

    /** 已发布（正常显示） */
    public static final Integer PUBLISHED = 1;

    /** 已隐藏（用户删除或管理员下架） */
    public static final Integer HIDDEN = 2;

}