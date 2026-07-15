package com.medireserve.patient.service;

import com.medireserve.common.constant.CacheKeyConstants;
import com.medireserve.common.service.MultiLevelCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * 统一缓存失效服务
 *
 * 集中管理所有缓存失效操作，避免 @CacheEvict 散落在各处
 * 在数据变更（增删改）时调用相应方法清除缓存
 */
@Slf4j
@Service
public class CacheEvictService {

    @Autowired
    private MultiLevelCacheService multiLevelCacheService;

    // ==================== 字典缓存失效 ====================

    /**
     * 清除所有字典缓存（科室、职称）
     * 在医生审核通过或医生信息变更时调用
     */
    @CacheEvict(value = {"departments", "titles"}, allEntries = true)
    public void evictDictCache() {
        log.info("清除字典缓存（departments, titles）");
    }

    // ==================== 医生列表缓存失效 ====================

    /**
     * 清除所有医生列表缓存（全量清除）
     * 在医生新增、审核通过、信息变更时调用
     */
    public void evictAllDoctorsCache() {
        String pattern = CacheKeyConstants.getDoctorsPattern();
        multiLevelCacheService.evictAll(pattern);
        log.info("清除所有医生列表缓存，pattern: {}", pattern);
    }

    // ==================== 排班缓存失效 ====================

    /**
     * 清除某个医生的所有排班缓存
     * 在排班新增、修改、删除，或预约创建/取消时调用
     */
    public void evictSchedulesByDoctor(Long doctorId) {
        if (doctorId == null) return;
        String pattern = CacheKeyConstants.getSchedulesPattern(doctorId);
        multiLevelCacheService.evictAll(pattern);
        log.info("清除医生 {} 的排班缓存，pattern: {}", doctorId, pattern);
    }

    // ==================== 医生信息缓存失效 ====================

    /**
     * 清除单个医生信息缓存
     */
    public void evictDoctorInfo(Long doctorId) {
        if (doctorId == null) return;
        String key = CacheKeyConstants.buildDoctorKey(doctorId);
        multiLevelCacheService.evict(key);
        log.info("清除医生信息缓存，key: {}", key);
    }

    // ==================== 组合失效 ====================

    /**
     * 医生信息变更时清除所有相关缓存
     */
    public void evictAllDoctorRelated(Long doctorId) {
        if (doctorId == null) return;
        evictDictCache();              // 科室列表（医生数量变化）
        evictAllDoctorsCache();        // 医生列表
        evictDoctorInfo(doctorId);     // 医生信息
        evictSchedulesByDoctor(doctorId); // 排班列表
        log.info("清除医生 {} 的所有相关缓存", doctorId);
    }
}