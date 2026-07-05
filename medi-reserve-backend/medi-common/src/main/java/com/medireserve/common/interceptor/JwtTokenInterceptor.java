package com.medireserve.common.interceptor;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;
import com.medireserve.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取 token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            log.warn("请求路径 {} 未携带 token", request.getRequestURI());
            response.setStatus(StatusCodeConstant.UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":" + StatusCodeConstant.UNAUTHORIZED + ",\"msg\":\"" + MessageConstant.TOKEN_MISSING + "\"}");
            return false;
        }

        // 2. 去掉 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 3. 解析令牌（静态调用）
            Claims claims = JwtUtil.parseToken(token);

            // 4. 提取用户信息存入 request 属性
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("username", claims.get("username"));
            request.setAttribute("role", claims.get("role"));

            log.debug("JWT 校验通过，用户: {}，角色: {}", claims.get("username"), claims.get("role"));
            return true;

        } catch (Exception e) {
            log.error("JWT 校验失败: {}", e.getMessage());
            response.setStatus(StatusCodeConstant.UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":" + StatusCodeConstant.UNAUTHORIZED + ",\"msg\":\"" + MessageConstant.TOKEN_INVALID + "\"}");
            return false;
        }
    }
}