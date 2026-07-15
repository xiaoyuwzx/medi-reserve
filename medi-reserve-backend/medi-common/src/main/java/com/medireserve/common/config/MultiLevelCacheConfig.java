package com.medireserve.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 多级缓存配置
 *
 * 注意：本配置只负责 Caffeine 本地缓存管理器，
 * Redis 缓存管理器由 CacheConfig 提供，并通过 @Primary 区分
 *
 * 缓存层级：
 * - Caffeine（本地缓存）：用于热点数据，访问速度快
 * - Redis（分布式缓存）：由 CacheConfig 提供，用于跨实例共享
 */
@Configuration
@EnableCaching
public class MultiLevelCacheConfig {

    /**
     * Caffeine 缓存管理器（作为默认缓存管理器）
     * @Cacheable 注解默认使用此管理器
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .recordStats()
        );
        cacheManager.setAllowNullValues(true);  // 允许空值缓存（防穿透）
        return cacheManager;
    }
}