package com.medireserve.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    // 排除无需记录的路径
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator/health",
            "/favicon.ico",
            "/doc.html",
            "/swagger-ui",
            "/webjars",
            "/v3/api-docs"
    );

    // 敏感字段，日志中替换为 ***
    private static final List<String> SENSITIVE_FIELDS = List.of(
            "password", "oldPassword", "newPassword", "confirmPassword",
            "token", "accessKey", "secretKey", "idCard"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        // 包装 request 和 response，以便读取 body（多次读取）
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(req);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(res);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = responseWrapper.getStatus();

            // 获取请求参数（拼接 query string + body）
            String queryString = req.getQueryString();
            String body = getRequestBody(requestWrapper);

            // 过滤敏感字段（将密码等替换为 ***）
            if (body != null && !body.isEmpty()) {
                for (String field : SENSITIVE_FIELDS) {
                    body = body.replaceAll("\"" + field + "\"\\s*:\\s*\"[^\"]*\"", "\"" + field + "\":\"***\"");
                }
            }

            // 记录日志
            log.info("请求 {} {} 状态码 {} 耗时 {}ms 参数: {} 请求体: {}",
                    req.getMethod(), path, status, duration,
                    queryString != null ? queryString : "",
                    body != null ? body : "");

            // 必须调用，将缓存的响应体写回客户端
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * 从 ContentCachingRequestWrapper 中读取请求体（JSON 字符串）
     */
    private String getRequestBody(ContentCachingRequestWrapper wrapper) {
        byte[] content = wrapper.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        try {
            return new String(content, wrapper.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}