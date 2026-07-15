package com.medireserve.websocket.interceptor;

import com.medireserve.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手认证拦截器
 *
 * 核心功能：
 * 1. 从 URL 参数中提取 JWT Token 并解析用户信息
 * 2. 从 URL 参数中提取 appointmentId（用于自动加入问诊室）
 * 3. 将用户信息存入 WebSocket Session 属性，供后续业务使用
 *
 * 前端连接示例：/ws/chat?token=xxx&appointmentId=1001
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

            // ========== 1. 从 Query String 获取 Token ==========
            String token = servletRequest.getServletRequest().getParameter("token");
            if (!StringUtils.hasText(token)) {
                log.warn("WebSocket 握手失败：未携带 Token");
                return false;
            }

            // ========== 2. 从 Query String 获取 appointmentId（用于自动加入房间） ==========
            String appointmentIdParam = servletRequest.getServletRequest().getParameter("appointmentId");
            if (StringUtils.hasText(appointmentIdParam)) {
                try {
                    attributes.put("appointmentId", Long.parseLong(appointmentIdParam));
                } catch (NumberFormatException e) {
                    log.warn("appointmentId 参数格式错误：{}，将不自动加入房间", appointmentIdParam);
                }
            }

            // ========== 3. 解析 JWT Token ==========
            try {
                Claims claims = JwtUtil.parseToken(token);
                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);

                if (userId == null || role == null) {
                    log.warn("WebSocket 握手失败：Token 中缺少 userId 或 role");
                    return false;
                }

                // ========== 4. 存入 Session 属性 ==========
                attributes.put("userId", userId);
                attributes.put("role", role);

                log.info("WebSocket 握手成功，用户ID：{}，角色：{}，预约ID：{}",
                        userId, role, attributes.get("appointmentId"));
                return true;

            } catch (Exception e) {
                log.warn("WebSocket 握手失败：Token 无效或已过期，{}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后无需额外处理
    }
}