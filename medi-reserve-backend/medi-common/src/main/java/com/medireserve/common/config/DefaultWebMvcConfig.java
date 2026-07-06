package com.medireserve.common.config;

import com.medireserve.common.interceptor.JwtTokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 默认的 Spring MVC 配置：注册 JWT 拦截器。
 * 特点：子模块可通过 application.yml 覆盖放行路径，也可自己写配置类完全替换。
 */
@Slf4j
@Configuration
public class DefaultWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    @Autowired
    private JwtInterceptorProperties jwtInterceptorProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 从配置属性中获取放行路径列表
        List<String> excludePaths = jwtInterceptorProperties.getExcludePaths();
        log.info("JWT 拦截器放行路径: {}", excludePaths);
        // 如果配置文件中未定义，则使用默认值（保证向后兼容）
        if (excludePaths == null || excludePaths.isEmpty()) {
            excludePaths = List.of(
                    "/auth/login", "/auth/register", "/error",
                    "/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**"
            );
        }
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePaths);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}