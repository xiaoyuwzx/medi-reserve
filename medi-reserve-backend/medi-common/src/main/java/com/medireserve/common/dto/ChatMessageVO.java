package com.medireserve.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息返回 VO
 * 用于展示历史记录和实时消息推送
 */
@Data
public class ChatMessageVO {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者姓名（用于前端展示头像/昵称）
     */
    private String senderName;

    /**
     * 发送者角色（PATIENT/DOCTOR，用于展示不同样式）
     */
    private String senderRole;

    /**
     * 消息内容（已过滤 XSS）
     */
    private String content;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 是否是自己发的（前端根据此字段控制气泡方向）
     */
    private Boolean isSelf;
}