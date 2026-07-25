package com.medireserve.admin.aspect;

import cn.hutool.json.JSONUtil;
import com.medireserve.admin.service.OperationLogService;
import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.entity.OperationLog;
import com.medireserve.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 操作日志 AOP 切面
 * 拦截所有标注了 @LogOperation 注解的方法，自动记录操作日志
 *
 * 核心机制：环绕通知（@Around）
 * - 在目标方法执行前：收集请求信息、开始计时
 * - 在目标方法执行后：记录耗时、结果状态
 * - 无论成功/失败：finally 块保证日志必定保存
 *
 * 为什么用 Around 而不是 AfterReturning + AfterThrowing？
 * - 需要记录执行耗时（必须捕获前后时间差）
 * - 需要统一处理成功和失败两种状态
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 环绕通知：拦截所有标注了 @LogOperation 的方法
     *
     * 切入点表达式：@annotation(com.medireserve.common.annotation.LogOperation)
     * 只要方法上有这个注解，就会进入此通知
     */
    @Around("@annotation(com.medireserve.common.annotation.LogOperation)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取当前请求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 非 Web 请求（如定时任务），不记录
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 2. 获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogOperation logOperation = method.getAnnotation(LogOperation.class);

        // 3. 构建日志对象
        OperationLog logEntity = new OperationLog();
        logEntity.setModule(logOperation.module());
        logEntity.setOperation(logOperation.operation());
        logEntity.setMethod(request.getMethod());
        logEntity.setPath(request.getRequestURI());
        logEntity.setIp(getClientIp(request));

        // 4. 获取当前管理员 ID 和名称（从请求属性中获取，由 JWT 拦截器存入）
        Number userIdObj = (Number) request.getAttribute("userId");
        Long adminId = userIdObj != null ? userIdObj.longValue() : null;
        String adminName = (String) request.getAttribute("username");

        if (adminId != null) {
            logEntity.setAdminId(adminId);
            logEntity.setAdminName(adminName != null ? adminName : "未知管理员");
        } else {
            // 如果无法获取管理员信息（可能未登录），但仍记录（如登录接口本身）
            logEntity.setAdminId(0L);
            logEntity.setAdminName("系统");
        }

        // 5. 记录请求参数（如果允许）
        if (logOperation.recordParams()) {
            Object[] args = joinPoint.getArgs();
            // 过滤掉 HttpServletRequest/Response 等无法序列化的参数
            String params = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest)
                            && !(arg instanceof HttpServletResponse))
                    .map(JSONUtil::toJsonStr)   // 使用 Hutool 的 JSONUtil
                    .collect(Collectors.joining(", "));
            logEntity.setParams(params);
        }

        // 6. 执行目标方法，并记录耗时和结果
        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        String errorMsg = null;
        int statusCode = 200;

        try {
            result = joinPoint.proceed();
            // 如果方法返回 Result 对象，判断其 code 是否成功
            if (result instanceof Result) {
                Result<?> r = (Result<?>) result;
                if (r.getCode() != 1) {
                    success = false;
                    errorMsg = r.getMsg();
                    statusCode = r.getCode() != null ? r.getCode() : 500;
                }
            }
            // 若返回 void 或非 Result，默认成功
        } catch (Exception e) {
            // 业务异常（由全局异常处理器处理），记录失败状态
            success = false;
            errorMsg = e.getMessage();
            statusCode = 500;
            throw e; // 继续向上抛出，不影响业务异常处理
        } finally {
            // 填充日志剩余字段
            long duration = System.currentTimeMillis() - startTime;
            logEntity.setResult(success ? 1 : 0);
            logEntity.setStatusCode(statusCode);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setDurationMs((int) duration);

            // 7. 异步保存日志（避免影响主业务性能）
            // 关键：调用异步方法，主线程立即返回，不等待日志写入
            operationLogService.saveLogAsync(logEntity);
        }

        return result;
    }

    /**
     * 获取客户端真实 IP（考虑代理和负载均衡场景）
     *
     * 读取顺序：
     * X-Forwarded-For → Proxy-Client-IP → WL-Proxy-Client-IP → RemoteAddr
     *
     * 如果通过 Nginx 代理，X-Forwarded-For 会记录真实客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}