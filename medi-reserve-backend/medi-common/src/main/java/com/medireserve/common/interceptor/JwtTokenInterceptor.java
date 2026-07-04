package com.medireserve.common.interceptor;

import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;
import com.medireserve.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            log.warn("请求路径 {} 未携带 token", request.getRequestURI());
            response.setStatus(StatusCodeConstant.UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":" + StatusCodeConstant.UNAUTHORIZED + ",\"msg\":\"" + MessageConstant.TOKEN_MISSING + "\"}");
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = jwtUtil.parseToken(token);
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