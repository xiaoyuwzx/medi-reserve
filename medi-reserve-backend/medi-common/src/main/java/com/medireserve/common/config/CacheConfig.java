package com.medireserve.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Spring Cache + Redis 缓存配置
 * 启用 @Cacheable、@CacheEvict 等注解
 * 不同缓存设置不同的过期时间
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 创建支持 Java 8 时间类型的 JSON 序列化器
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册 JavaTimeModule 以支持 LocalDateTime、LocalDate 等
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期序列化为时间戳，使用 ISO-8601 格式（可选）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    private RedisCacheConfiguration createCacheConfig(Duration ttl, boolean allowNull) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(createJsonSerializer())
                );
        if (!allowNull) {
            config = config.disableCachingNullValues();
        }
        return config;
    }


    /**
     * 配置 Redis 缓存管理器
     * 设置默认过期时间，并使用 JSON 序列化（方便查看缓存内容）
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(createCacheConfig(Duration.ofMinutes(10), false))
                // 科室列表：变化极少，缓存1小时
                .withCacheConfiguration("departments", createCacheConfig(Duration.ofHours(1), true))
                // 职称列表：变化极少，缓存1小时
                .withCacheConfiguration("titles", createCacheConfig(Duration.ofHours(1), true))
                // 医生列表：变化较少，缓存5分钟
                .withCacheConfiguration("doctors", createCacheConfig(Duration.ofMinutes(5), false))
                // 排班日历：允许缓存空值5分钟，防止缓存穿透
                .withCacheConfiguration("schedules", createCacheConfig(Duration.ofMinutes(1), true))
                .build();
    }
}