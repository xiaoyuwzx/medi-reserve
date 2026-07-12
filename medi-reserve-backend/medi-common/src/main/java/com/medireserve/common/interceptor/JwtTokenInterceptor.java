package com.medireserve.common.interceptor;

import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;
import com.medireserve.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非控制器方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 从请求头获取 token
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
            Claims claims = JwtUtil.parseToken(token);
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("username", claims.get("username"));
            request.setAttribute("role", claims.get("role"));

            // 检查方法上是否有 @RequireRole 注解
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRole != null) {
                String[] allowedRoles = requireRole.value();
                String currentRole = claims.get("role", String.class);
                boolean hasPermission = Arrays.asList(allowedRoles).contains(currentRole);
                if (!hasPermission) {
                    log.warn("权限不足，当前角色：{}，需要角色：{}", currentRole, Arrays.toString(allowedRoles));
                    response.setStatus(StatusCodeConstant.FORBIDDEN);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"code\":" + StatusCodeConstant.FORBIDDEN + ",\"msg\":\"" + MessageConstant.PERMISSION_DENIED + "\"}");
                    return false;
                }
            }

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