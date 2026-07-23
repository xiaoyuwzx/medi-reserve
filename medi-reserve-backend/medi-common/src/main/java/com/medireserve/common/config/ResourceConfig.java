package com.medireserve.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 注意：此配置为 Knife4j 辅助配置，Spring Boot 默认已支持 /webjars/**
        // 若升级 knife4j 版本，请同步修改下方路径中的版本号（如 4.5.0 -> 4.6.0）
        // 将 /css/** 映射到 Knife4j 的静态资源位置（通常在 webjars 中）
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/knife4j-openapi3-ui/4.5.0/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/knife4j-openapi3-ui/4.5.0/js/");
        // 如果需要 favicon.ico，也可以映射
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/META-INF/resources/webjars/knife4j-openapi3-ui/4.5.0/favicon.ico");
    }
}