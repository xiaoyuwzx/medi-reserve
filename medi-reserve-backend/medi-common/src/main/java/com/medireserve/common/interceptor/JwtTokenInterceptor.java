package com.medireserve.common.interceptor;

import com.medireserve.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 校验拦截器：验证请求头中的 Authorization 令牌
 */
@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取 token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            log.warn("请求路径 {} 未携带 token", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，请先登录\"}");
            return false;
        }

        // 2. 去掉 "Bearer " 前缀（如果有）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 3. 解析令牌
            Claims claims = jwtUtil.parseToken(token);

            // 4. 提取用户信息存入 request 属性（后续 Controller 可用 @RequestAttribute 获取）
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            log.info("JWT 校验通过，用户: {}，角色: {}，ID: {}", username, role, userId);
            return true;

        } catch (Exception e) {
            log.error("JWT 校验失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token 无效或已过期\"}");
            return false;
        }
    }
}