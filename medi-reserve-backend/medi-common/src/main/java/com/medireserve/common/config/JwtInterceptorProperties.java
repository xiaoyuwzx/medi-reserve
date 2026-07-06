package com.medireserve.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JWT 拦截器配置属性
 * 对应 application.yml 中的 jwt.interceptor 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt.interceptor")
public class JwtInterceptorProperties {

    /**
     * 是否启用拦截器
     */
    private Boolean enabled = true;

    /**
     * 放行路径列表（不需要 token 验证的路径）
     */
    private List<String> excludePaths;
}