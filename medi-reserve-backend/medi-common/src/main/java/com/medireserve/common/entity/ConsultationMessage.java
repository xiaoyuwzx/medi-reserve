package com.medireserve.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问诊聊天消息实体类
 * 对应数据库表 consultation_message
 */
@Data
public class ConsultationMessage {

    /**
     * 消息ID（主键自增）
     */
    private Long id;

    /**
     * 预约ID（关联 appointment 表）
     * 用于区分不同问诊室的消息
     */
    private Long appointmentId;

    /**
     * 发送者ID（关联 patient.id 或 doctor.id）
     */
    private Long senderId;

    /**
     * 接收者ID（关联 patient.id 或 doctor.id）
     */
    private Long receiverId;

    /**
     * 发送者角色：PATIENT 或 DOCTOR
     * 用于前端展示不同样式和查询时区分姓名来源
     */
    private String senderRole;

    /**
     * 消息内容（已过滤 XSS）
     * 最大长度 1000 字符
     */
    private String content;

    /**
     * 消息类型：1-文本，2-图片（预留）
     * 使用 MessageTypeConstant 常量
     */
    private Integer msgType;

    /**
     * 发送时间（默认当前时间）
     */
    private LocalDateTime sendTime;

}