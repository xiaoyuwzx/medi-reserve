package com.medireserve.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存门面服务
 *
 * 职责：
 * 1. 提供 L1（Caffeine 本地缓存）+ L2（Redis 分布式缓存）的透明读写
 * 2. 支持泛型反序列化（解决 PageInfo 等复杂类型的缓存问题）
 * 3. 提供缓存穿透防护（空值缓存）
 * 4. 提供按模式批量清除缓存（SCAN 替代 KEYS）
 *
 * 缓存层级：
 * - L1（Caffeine）：极速，进程内，适合单机热点数据，最大 500 条，过期 10 分钟
 * - L2（Redis）：跨实例共享，持久化，适合分布式场景，TTL 由调用方指定
 */
@Slf4j
@Service
public class MultiLevelCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Caffeine 本地缓存（最大 500 条，写入后 10 分钟过期）
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(500)
            .recordStats()
            .build();

    // ==================== 核心方法（带 Type） ====================

    /**
     * 核心方法：多级缓存获取
     * 从多级缓存获取数据（带回源函数）- 支持类型安全反序列化
     * @param cacheKey       Redis 的 Key（如 "cache:doctors:内科:none:1:10"）
     * @param localCacheKey  Caffeine 的 Key（通常与 Redis 一样，但为了灵活可单独指定）
     * @param supplier       回源函数（查数据库的 Lambda）
     * @param ttl            Redis 过期时间（秒）
     * @param targetType     目标类型（解决泛型反序列化问题，如 PageInfo<DoctorListVO>）
     */
    public <T> T get(String cacheKey, String localCacheKey, Supplier<T> supplier, Long ttl, Type targetType) {
        String localKey = localCacheKey != null ? localCacheKey : cacheKey;

        // 1. 尝试从 Caffeine 获取
        Object cachedLocal = localCache.getIfPresent(localKey);
        if (cachedLocal != null) {
            // 如果本地缓存存的是 LinkedHashMap（来自 Redis 反序列化），需转成目标类型
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
            // Redis 存储的也是 JSON，反序列化时如果是 LinkedHashMap，需转换
            if (targetType != null && cachedRedis instanceof LinkedHashMap) {
                result = convertToTargetType(cachedRedis, targetType);
            } else {
                result = (T) cachedRedis;
            }
            // 回填到 Caffeine，下次更快
            localCache.put(localKey, result);
            return result;
        }

        // 3. 回源查询
        log.debug("缓存未命中，回源查询，key: {}", cacheKey);
        T result = supplier.get();
        if (result != null) {
            // 写入 Redis（带 TTL）
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(cacheKey, result, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(cacheKey, result);
            }
            // 写入 Caffeine
            localCache.put(localKey, result);
        } else {
            // 为了避免缓存穿透，即使是 null 也缓存 10 分钟（空值缓存）
            redisTemplate.opsForValue().set(cacheKey, null, 10, TimeUnit.MINUTES);
            localCache.put(localKey, null);
        }

        return result;
    }

    // ==================== 新增：4 参数重载 ====================

    /**
     * 从多级缓存获取数据（带回源函数）- 不需要类型转换时使用
     *
     * @param cacheKey       Redis Key
     * @param localCacheKey  Caffeine Key
     * @param supplier       回源函数
     * @param ttl            Redis 过期时间
     * @param <T>            返回类型
     * @return 缓存数据
     */
    public <T> T get(String cacheKey, String localCacheKey, Supplier<T> supplier, Long ttl) {
        return get(cacheKey, localCacheKey, supplier, ttl, null);
    }

    // ==================== 3 参数重载 ====================

    /**
     * 使用同一 Key 作为 Redis 和 Caffeine 的 Key
     */
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

    /**
     * 将 LinkedHashMap 转换为目标类型（解决泛型反序列化问题）
     *
     * 为什么需要这个方法？
     * - 当 JSON 反序列化时，Jackson 不知道泛型的具体类型，只能把它转成 LinkedHashMap
     * - 例如：PageInfo<DoctorListVO> 被反序列化为 LinkedHashMap
     * - 这个方法利用 Jackson 的 TypeFactory，根据传入的 targetType 重新反序列化
     *
     * @param obj        LinkedHashMap 对象（来自 Redis 或 Caffeine）
     * @param targetType 目标类型（如 PageInfo<DoctorListVO> 的 Type）
     * @param <T>        返回类型
     * @return 转换后的目标类型对象
     */
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

    /**
     * 手动写入缓存（直接覆盖，不经过回源）
     *
     * 使用场景：
     * - 预置缓存（如启动预热）
     * - 更新缓存（当数据变更时，主动推送到 Redis 而非等待被动过期）
     *
     * @param cacheKey 缓存 Key
     * @param value    缓存值（可为 null）
     * @param ttl      过期时间（秒），null 表示永不过期
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
            redisTemplate.opsForValue().set(cacheKey, null, 5, TimeUnit.MINUTES);
            localCache.put(cacheKey, null);
        }
    }

    /**
     * 清除单条缓存（L1 + L2 同步删除）
     *
     * 使用场景：
     * - 单条数据变更时，精确删除该 Key
     * - 例如：某个医生信息变更，只删除该医生的缓存
     *
     * @param cacheKey 缓存 Key
     */
    public void evict(String cacheKey) {
        redisTemplate.delete(cacheKey);
        localCache.invalidate(cacheKey);
    }

    /**
     * 按模式批量清除缓存（L1 + L2 同步删除）
     *
     * 核心优化：使用 SCAN 替代 KEYS 命令，避免 Redis 阻塞
     *
     * 实现细节：
     * 1. L2（Redis）：使用 SCAN 游标 + 批量 DEL（每批 1000 个），不会阻塞 Redis
     * 2. L1（Caffeine）：按前缀匹配删除，避免误删其他缓存
     *
     * 使用场景：
     * - 医生列表缓存：所有分页组合都要清除（cache:doctors:*）
     * - 排班缓存：某医生的所有排班都要清除（cache:schedules:123:*）
     *
     * @param pattern 匹配模式（如 "cache:doctors:*" 或 "cache:schedules:123:*"）
     */
    public void evictAll(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            log.warn("evictAll 收到空 pattern，跳过缓存清除操作");
            return;
        }

        try {
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                List<byte[]> batchKeys = new ArrayList<>(1000);
                try (Cursor<byte[]> cursor = connection.scan(
                        ScanOptions.scanOptions()
                                .match(pattern)
                                .count(100)
                                .build())) {
                    while (cursor.hasNext()) {
                        batchKeys.add(cursor.next());
                        if (batchKeys.size() >= 1000) {
                            connection.del(batchKeys.toArray(new byte[0][]));
                            batchKeys.clear();
                        }
                    }
                    if (!batchKeys.isEmpty()) {
                        connection.del(batchKeys.toArray(new byte[0][]));
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Redis SCAN 批量删除缓存失败，pattern: {}", pattern, e);
            throw new RuntimeException("缓存清理失败", e);
        }

        String prefix = pattern.contains("*") ? pattern.substring(0, pattern.indexOf('*')) : pattern;
        localCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
        log.info("按模式清除缓存完成，pattern: {}，已清除本地缓存前缀: {}", pattern, prefix);
    }

    /**
     * 获取本地缓存（Caffeine）统计信息
     *
     * 返回内容示例：
     * "Cache{stats={hitCount=1234, missCount=56, loadSuccessCount=89, totalLoadTime=456789, ...}}"
     *
     * 使用场景：
     * - 运维监控：通过管理接口查看缓存命中率
     * - 性能调优：判断是否需要调整缓存大小或过期时间
     *
     * @return Caffeine 统计信息的字符串表示
     */
    public String getLocalCacheStats() {
        return localCache.stats().toString();
    }

}