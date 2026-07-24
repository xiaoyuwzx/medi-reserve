package com.medireserve.common.interceptor;

import cn.hutool.json.JSONUtil;
import com.medireserve.common.annotation.RequirePermission;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.StatusCodeConstant;
import com.medireserve.common.result.Result;
import com.medireserve.common.service.PermissionService;
import com.medireserve.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * JWT 认证 + 权限校验拦截器
 * 支持 @RequireRole 和 @RequirePermission 两种注解
 * 1. 判断需不需要登录（检查有无 @RequireRole/@RequirePermission）
 * 2. 需要的话，解析 JWT Token，校验合法性
 * 3. 校验通过后，把 userId 和 role 存入 request 属性（供 Controller 直接用）
 * 4. 校验角色的权限是否匹配（RBAC）
 */
@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    // PermissionService 允许为 null（因为患者/医生模块不引入该依赖，这里用 required=false）
    @Autowired(required = false)
    private PermissionService permissionService;  // 可选注入，兼容无权限模块

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 非控制器方法直接放行（如静态资源）
        // 只拦截 Controller 方法，静态资源（如 /doc.html）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String requestURI = request.getRequestURI();

        // 2. 检查是否需要认证（方法或类上是否有 @RequireRole 或 @RequirePermission）
        boolean requireAuth = handlerMethod.getMethodAnnotation(RequireRole.class) != null
                || handlerMethod.getBeanType().getAnnotation(RequireRole.class) != null
                || handlerMethod.getMethodAnnotation(RequirePermission.class) != null
                || handlerMethod.getBeanType().getAnnotation(RequirePermission.class) != null;

        if (!requireAuth) {
            log.debug("无需认证，放行：{}", requestURI);
            return true;
        }

        // 3. 提取 Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            log.warn("请求路径 {} 未携带 Token", requestURI);
            handleUnauthorized(response, MessageConstant.TOKEN_MISSING);
            return false;
        }
        // 前端习惯带 "Bearer " 前缀，剥掉它
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 4. 解析 Token
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (Exception e) {
            log.error("JWT 校验失败: {}", e.getMessage());
            handleUnauthorized(response, MessageConstant.TOKEN_INVALID);
            return false;
        }

        // 5. 提取用户信息（安全转换，避免 ClassCastException）
        // 从 Claims 中取用户信息，存到 request 属性中
        Object userIdObj = claims.get("userId");
        // JWT 里的 userId 可能是 Integer 或 Long，用 Number 兼容取出来
        Long userId = userIdObj instanceof Number ? ((Number) userIdObj).longValue() : null;
        String username = claims.get("username", String.class);
        String roleName = claims.get("role", String.class);

        if (userId == null) {
            log.warn("Token 中缺少 userId");
            handleUnauthorized(response, MessageConstant.TOKEN_INVALID);
            return false;
        }

        // 存入 request 属性（供业务层使用）
        // 把这些信息塞进 request 作用域，后续 Controller 用 @RequestAttribute("userId") 就能拿到
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", roleName);

        // 6. 打印调试信息
        log.debug("JWT 校验通过，用户：{}，角色：{}", username, roleName);

        // 7. 权限校验
        // 7.1 @RequireRole 校验
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            // 如果方法上没有，检查类上是否有
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null) {
            String[] allowedRoles = requireRole.value();
            boolean roleMatch = Arrays.asList(allowedRoles).contains(roleName);
            if (!roleMatch) {
                log.warn("角色权限不足，当前角色：{}，需要角色：{}", roleName, Arrays.toString(allowedRoles));
                handleForbidden(response, MessageConstant.PERMISSION_DENIED);
                return false;
            }
        }

        // 7.2 @RequirePermission 校验
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (requirePermission != null) {
            // 检查 permissionService 是否可用（未注入时跳过，适用于患者/医生端）
            if (permissionService == null) {
                log.warn("PermissionService 未注入，跳过权限校验");
            } else {
                String requiredPerm = requirePermission.value();
                // 根据角色名获取角色ID，再查询权限
                Integer roleId = getRoleId(roleName);
                Set<String> permissionCodes = permissionService.getPermissionCodesByRoleId(roleId);
                if (!permissionCodes.contains(requiredPerm)) {
                    log.warn("缺少权限，用户：{}，需要权限：{}", username, requiredPerm);
                    handleForbidden(response, "缺少权限：" + requiredPerm);
                    return false;
                }
                log.debug("权限校验通过，权限：{}", requiredPerm);
            }
        }

        return true;
    }

    /**
     * 将角色名称转换为角色ID（与 role 表一致）
     */
    private Integer getRoleId(String roleName) {
        if (roleName == null) {
            return 0;
        }
        switch (roleName) {
            case "SUPER_ADMIN":
                return 1;
            case "ADMIN":
                return 2;
            default:
                return 0;  // 患者/医生或无权限角色
        }
    }

    /**
     * 处理未授权（401）
     */
    private void handleUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.error(StatusCodeConstant.UNAUTHORIZED, msg)));
    }

    /**
     * 处理禁止访问（403）
     */
    private void handleForbidden(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.error(StatusCodeConstant.FORBIDDEN, msg)));
    }
}