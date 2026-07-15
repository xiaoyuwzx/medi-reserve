package com.medireserve.websocket.config;

import com.medireserve.websocket.interceptor.WebSocketHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP 协议配置
 * 启用消息代理，配置端点与拦截器
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketHandshakeInterceptor handshakeInterceptor;

    /**
     * 注册 STOMP 端点（前端连接入口）
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")                // WebSocket 连接地址
                .setAllowedOriginPatterns("*")          // 允许跨域（开发环境）
                .addInterceptors(handshakeInterceptor)  // 注册认证拦截器
                .withSockJS();                          // 开启 SockJS 支持（兼容不支持 WebSocket 的浏览器）
    }

    /**
     * 配置消息代理（转发消息的中间人）
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 设置应用前缀（客户端发送消息到 /app/xxx，由 @MessageMapping 处理）
        registry.setApplicationDestinationPrefixes("/app");

        // 2. 设置广播/点对点前缀（客户端订阅 /user/queue/xxx 接收消息）
        // 开启 /user 前缀支持点对点消息（自动根据 Principal 转发）
        registry.enableSimpleBroker("/user", "/topic");

        // 3. 设置点对点消息的前缀（默认 /user）
        registry.setUserDestinationPrefix("/user");
    }
}