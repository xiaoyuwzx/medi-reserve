package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息返回 VO
 * 用于展示历史记录和实时消息推送
 */
@Data
@Schema(description = "聊天消息返回 VO")
public class ChatMessageVO {

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者姓名")
    private String senderName;

    @Schema(description = "发送者角色：PATIENT/DOCTOR")
    private String senderRole;

    @Schema(description = "消息内容（已过滤XSS）")
    private String content;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "是否为自己发送（前端控制气泡方向）")
    private Boolean isSelf;
}