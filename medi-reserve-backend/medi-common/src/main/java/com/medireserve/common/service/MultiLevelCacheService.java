package com.medireserve.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class MultiLevelCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(500)
            .recordStats()
            .build();

    // ==================== 核心方法（带 Type） ====================

    /**
     * 从多级缓存获取数据（带回源函数）- 支持类型安全反序列化
     */
    public <T> T get(String cacheKey, String localCacheKey, Supplier<T> supplier, Long ttl, Type targetType) {
        String localKey = localCacheKey != null ? localCacheKey : cacheKey;

        // 1. 尝试从 Caffeine 获取
        Object cachedLocal = localCache.getIfPresent(localKey);
        if (cachedLocal != null) {
            log.debug("本地缓存命中，key: {}", localKey);
            if (targetType != null && cachedLocal instanceof LinkedHashMap) {
                return convertToTargetType(cachedLocal, targetType);
            }
            return (T) cachedLocal;
        }

        // 2. 尝试从 Redis 获取
        Object cachedRedis = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRedis != null) {
            log.debug("Redis 缓存命中，key: {}", cacheKey);
            T result;
            if (targetType != null && cachedRedis instanceof LinkedHashMap) {
                result = convertToTargetType(cachedRedis, targetType);
            } else {
                result = (T) cachedRedis;
            }
            localCache.put(localKey, result);
            return result;
        }

        // 3. 回源查询
        log.debug("缓存未命中，回源查询，key: {}", cacheKey);
        T result = supplier.get();
        if (result != null) {
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(cacheKey, result, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(cacheKey, result);
            }
            localCache.put(localKey, result);
        } else {
            redisTemplate.opsForValue().set(cacheKey, null, 10, TimeUnit.MINUTES);
            localCache.put(localKey, null);
        }

        return result;
    }

    // ==================== 新增：4 参数重载 ====================

    /**
     * 从多级缓存获取数据（带回源函数）- 不需要类型转换时使用
     */
    public <T> T get(String cacheKey, String localCacheKey, Supplier<T> supplier, Long ttl) {
        return get(cacheKey, localCacheKey, supplier, ttl, null);
    }

    // ==================== 3 参数重载 ====================

    public <T> T get(String cacheKey, Supplier<T> supplier, Long ttl, Type targetType) {
        return get(cacheKey, cacheKey, supplier, ttl, targetType);
    }

    public <T> T get(String cacheKey, Supplier<T> supplier, Long ttl) {
        return get(cacheKey, cacheKey, supplier, ttl, null);
    }

    public <T> T get(String cacheKey, Supplier<T> supplier, Type targetType) {
        return get(cacheKey, cacheKey, supplier, null, targetType);
    }

    public <T> T get(String cacheKey, Supplier<T> supplier) {
        return get(cacheKey, cacheKey, supplier, null, null);
    }

    // ==================== 工具方法 ====================

    private <T> T convertToTargetType(Object obj, Type targetType) {
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, typeFactory.constructType(targetType));
        } catch (JsonProcessingException e) {
            log.error("类型转换失败，目标类型：{}", targetType, e);
            throw new RuntimeException("缓存数据格式转换失败", e);
        }
    }

    public void put(String cacheKey, Object value, Long ttl) {
        if (value != null) {
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(cacheKey, value, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(cacheKey, value);
            }
            localCache.put(cacheKey, value);
        } else {
            redisTemplate.opsForValue().set(cacheKey, null, 5, TimeUnit.MINUTES);
            localCache.put(cacheKey, null);
        }
    }

    public void evict(String cacheKey) {
        redisTemplate.delete(cacheKey);
        localCache.invalidate(cacheKey);
    }

    public void evictAll(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        localCache.invalidateAll();
    }

    public String getLocalCacheStats() {
        return localCache.stats().toString();
    }
}