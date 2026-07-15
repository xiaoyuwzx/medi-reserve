package com.medireserve.common.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存服务
 *
 * 提供手动操作多级缓存的方法：
 * - get：先查 Caffeine，再查 Redis，最后查数据库
 * - put：写入 Redis 和 Caffeine
 * - evict：清除所有层缓存
 *
 * 使用场景：需要手动控制缓存的业务场景（如医生列表分页）
 */
@Slf4j
@Service
public class MultiLevelCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 本地缓存（用于热点数据）
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)  // 本地缓存10分钟
            .maximumSize(500)
            .recordStats()
            .build();

    /**
     * 从多级缓存获取数据（带回源函数）
     *
     * 流程：
     * 1. 先查 Caffeine 本地缓存
     * 2. 未命中则查 Redis
     * 3. 未命中则执行回源函数查询数据库
     * 4. 查询结果写入 Redis 和 Caffeine
     *
     * @param cacheKey 缓存Key（Redis Key）
     * @param localCacheKey 本地缓存Key（可自定义，默认与 cacheKey 相同）
     * @param supplier 回源函数（查询数据库）
     * @param ttl 缓存过期时间（秒），null 则使用默认
     * @return 缓存数据
     */
    public <T> T get(String cacheKey, String localCacheKey, Supplier<T> supplier, Long ttl) {
        String localKey = localCacheKey != null ? localCacheKey : cacheKey;

        // 1. 尝试从 Caffeine 获取
        Object cachedLocal = localCache.getIfPresent(localKey);
        if (cachedLocal != null) {
            log.debug("本地缓存命中，key: {}", localKey);
            return (T) cachedLocal;
        }

        // 2. 尝试从 Redis 获取
        Object cachedRedis = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRedis != null) {
            log.debug("Redis 缓存命中，key: {}", cacheKey);
            // 回写本地缓存
            localCache.put(localKey, cachedRedis);
            return (T) cachedRedis;
        }

        // 3. 回源查询
        log.debug("缓存未命中，回源查询，key: {}", cacheKey);
        T result = supplier.get();
        if (result != null) {
            // 写入 Redis
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(cacheKey, result, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(cacheKey, result);
            }
            // 写入本地缓存
            localCache.put(localKey, result);
        } else {
            // 空值缓存（防穿透）：缓存空对象，有效期5分钟
            redisTemplate.opsForValue().set(cacheKey, null, 5, TimeUnit.MINUTES);
            localCache.put(localKey, null);
        }

        return result;
    }

    /**
     * 重载：使用默认本地缓存Key（与 cacheKey 相同）
     */
    public <T> T get(String cacheKey, Supplier<T> supplier, Long ttl) {
        return get(cacheKey, cacheKey, supplier, ttl);
    }

    /**
     * 重载：不设置过期时间（使用 Redis 默认）
     */
    public <T> T get(String cacheKey, Supplier<T> supplier) {
        return get(cacheKey, cacheKey, supplier, null);
    }

    /**
     * 写入缓存（同时写入 Redis 和本地）
     */
    public void put(String cacheKey, Object value, Long ttl) {
        if (value != null) {
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(cacheKey, value, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(cacheKey, value);
            }
            localCache.put(cacheKey, value);
        } else {
            // 空值缓存（防穿透）
            redisTemplate.opsForValue().set(cacheKey, null, 5, TimeUnit.MINUTES);
            localCache.put(cacheKey, null);
        }
    }

    /**
     * 清除缓存（同时清除 Redis 和本地）
     */
    public void evict(String cacheKey) {
        redisTemplate.delete(cacheKey);
        localCache.invalidate(cacheKey);
        log.debug("缓存已清除，key: {}", cacheKey);
    }

    /**
     * 批量清除缓存（Redis 使用 pattern，本地清除所有）
     */
    public void evictAll(String pattern) {
        // 清除 Redis（使用 keys 操作，生产环境慎用，建议用 scan）
        // 为了安全，这里使用 keys，但生产环境应使用 scan
        // 这里仅用于演示
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        // 清除本地缓存（全部清除，因为无法按 pattern 清除）
        localCache.invalidateAll();
        log.debug("批量缓存已清除，pattern: {}", pattern);
    }

    /**
     * 获取本地缓存统计信息
     */
    public String getLocalCacheStats() {
        return localCache.stats().toString();
    }
}