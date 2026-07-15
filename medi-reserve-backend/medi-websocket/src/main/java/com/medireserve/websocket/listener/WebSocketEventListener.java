package com.medireserve.websocket.listener;

import com.medireserve.websocket.service.ConsultationRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 事件监听器
 *
 * 核心功能：
 * 1. 监听连接建立事件：标记用户在线、自动加入房间、推送离线消息
 * 2. 监听连接断开事件：标记用户离线、离开房间
 */
@Slf4j
@Component
public class WebSocketEventListener {

    @Autowired
    private ConsultationRedisService consultationRedisService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 监听连接建立事件
     * 触发时机：WebSocket 握手成功后
     *
     * 处理逻辑：
     * 1. 标记用户在线（Redis 存储 sessionId）
     * 2. 如果提供了 appointmentId，自动加入房间（用于在线人数统计）
     * 3. 推送离线消息（用户上线后自动接收之前未收到的消息）
     */
    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            Long userId = (Long) sessionAttributes.get("userId");
            String role = (String) sessionAttributes.get("role");
            Long appointmentId = (Long) sessionAttributes.get("appointmentId");
            String sessionId = accessor.getSessionId();

            if (userId != null) {
                // ========== 1. 标记用户在线 ==========
                consultationRedisService.userOnline(userId, sessionId);
                log.info("用户 {} 上线，角色：{}，sessionId：{}", userId, role, sessionId);

                // ========== 2. 自动加入问诊室（如果提供了 appointmentId） ==========
                if (appointmentId != null) {
                    consultationRedisService.joinRoom(appointmentId, userId);
                    log.info("用户 {} 加入问诊室：{}", userId, appointmentId);
                }

                // ========== 3. 推送离线消息 ==========
                List<Object> offlineMessages = consultationRedisService.getAndClearOfflineMessages(userId);
                if (offlineMessages != null && !offlineMessages.isEmpty()) {
                    for (Object msg : offlineMessages) {
                        // 逐条推送给用户
                        messagingTemplate.convertAndSendToUser(
                                userId.toString(),
                                "/queue/messages",
                                msg
                        );
                    }
                    log.info("用户 {} 上线，推送离线消息 {} 条", userId, offlineMessages.size());
                }
            }
        }
    }

    /**
     * 监听连接断开事件
     * 触发时机：WebSocket 连接关闭（用户主动关闭、网络超时等）
     *
     * 处理逻辑：
     * 1. 标记用户离线（删除 Redis 中的 sessionId）
     * 2. 如果提供了 appointmentId，离开房间（更新在线人数）
     */
    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            Long userId = (Long) sessionAttributes.get("userId");
            Long appointmentId = (Long) sessionAttributes.get("appointmentId");

            if (userId != null) {
                // ========== 1. 标记用户离线 ==========
                consultationRedisService.userOffline(userId);
                log.info("用户 {} 已标记离线", userId);

                // ========== 2. 离开问诊室（如果提供了 appointmentId） ==========
                if (appointmentId != null) {
                    consultationRedisService.leaveRoom(appointmentId, userId);
                    log.info("用户 {} 离开问诊室：{}", userId, appointmentId);
                }
            }
        }
    }
}