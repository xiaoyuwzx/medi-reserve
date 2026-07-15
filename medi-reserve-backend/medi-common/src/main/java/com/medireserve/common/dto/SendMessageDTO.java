package com.medireserve.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * WebSocket 发送消息的 DTO
 * 前端通过 STOMP 协议发送 JSON 时使用
 */
@Data
public class SendMessageDTO {

    /**
     * 预约ID（必填，用于确定聊天房间）
     */
    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    /**
     * 接收者ID（必填，决定消息推送给谁）
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息内容（必填，最大1000字符，防XSS过滤由后端处理）
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容不能超过1000字")
    private String content;

    /**
     * 消息类型（默认为文本，保留扩展）
     */
    private Integer msgType = 1;
}