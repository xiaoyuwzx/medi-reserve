package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * WebSocket 发送消息的 DTO
 * 前端通过 STOMP 协议发送 JSON 时使用
 */
@Data
@Schema(description = "WebSocket 发送消息 DTO")
public class SendMessageDTO {

    @Schema(description = "预约ID（用于确定聊天房间）", required = true)
    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    @Schema(description = "接收者ID", required = true)
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @Schema(description = "消息内容（最多1000字符）", required = true)
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容不能超过1000字")
    private String content;

    @Schema(description = "消息类型：1-文本，2-图片（预留）")
    private Integer msgType = 1;
}