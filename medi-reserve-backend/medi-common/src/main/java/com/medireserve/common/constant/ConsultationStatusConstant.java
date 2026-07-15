package com.medireserve.common.constant;

/**
 * 问诊室状态常量（用于业务校验）
 */
public class ConsultationStatusConstant {

    /**
     * 问诊进行中（允许收发消息）
     */
    public static final Integer ACTIVE = 1;

    /**
     * 问诊已结束（禁止收发消息）
     */
    public static final Integer ENDED = 0;
}