package com.medireserve.common.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置类
 * 为不同的业务数据创建独立的布隆过滤器实例
 *
 * 布隆过滤器用于快速判断一个元素是否「可能存在」或「一定不存在」，
 * 有效防止缓存穿透攻击（恶意请求不存在的ID直接打到数据库）
 */
@Configuration
public class BloomFilterConfig {

    /**
     * 预期插入数量：医生总数（假设最多 10000 人）
     * 误判率：1%（可接受范围）
     *
     * 内存占用估算：约 10000 * log2(e) * log2(1/0.01) ≈ 10000 * 1.44 * 6.64 ≈ 95KB
     */
    private static final int EXPECTED_DOCTORS = 10000;
    private static final double FPP = 0.01;  // 1% 误判率

    /**
     * 排班预期数量：假设每个医生每天最多 2 个排班，未来 30 天，最多 10000 医生
     * 10000 * 2 * 30 = 600000，但考虑清理，设 1000000
     */
    private static final int EXPECTED_SCHEDULES = 1000000;
    private static final double FPP_SCHEDULE = 0.01;

    /**
     * 医生ID布隆过滤器
     * 用于拦截不存在的医生ID查询
     */
    @Bean
    public BloomFilter<Long> doctorBloomFilter() {
        return BloomFilter.create(
                Funnels.longFunnel(),
                EXPECTED_DOCTORS,
                FPP
        );
    }

    /**
     * 排班ID布隆过滤器
     * 用于拦截不存在的排班ID查询
     */
    @Bean
    public BloomFilter<Long> scheduleBloomFilter() {
        return BloomFilter.create(
                Funnels.longFunnel(),
                EXPECTED_SCHEDULES,
                FPP_SCHEDULE
        );
    }
}