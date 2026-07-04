package com.medireserve.common.config;

import com.medireserve.common.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 默认的 Spring MVC 配置：注册 JWT 拦截器。
 * 特点：子模块可通过 application.yml 覆盖放行路径，也可自己写配置类完全替换。
 */
@Configuration
// 默认启用（如果子模块把 jwt.interceptor.enabled 设为 false 则关闭）
@ConditionalOnProperty(prefix = "jwt.interceptor", name = "enabled", havingValue = "true", matchIfMissing = true)
// 如果子模块已经自己定义了 WebMvcConfigurer 的 Bean，这个配置就不生效，避免冲突
@ConditionalOnMissingBean(WebMvcConfigurer.class)
public class DefaultWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    // 从配置文件读取放行路径（逗号分隔），默认放行登录、注册、测试接口
    @Value("${jwt.interceptor.exclude-paths:/auth/login,/auth/register,/test/ping,/error}")
    private String[] excludePaths;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")               // 拦截所有请求
                .excludePathPatterns(excludePaths);   // 放行指定路径
    }
}