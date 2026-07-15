package com.medireserve.common.constant;

/**
 * WebSocket 消息类型常量
 */
public class MessageTypeConstant {

    /**
     * 文本消息
     */
    public static final Integer TEXT = 1;

    /**
     * 图片消息（预留）
     */
    public static final Integer IMAGE = 2;

    /**
     * 系统提示（如：患者进入房间、医生结束问诊等）
     */
    public static final Integer SYSTEM = 3;
}