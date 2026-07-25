package com.medireserve.patient.service;

/**
 * 统一缓存失效服务
 *
 * 集中管理所有缓存失效操作，避免 @CacheEvict 散落在各处
 * 在数据变更（增删改）时调用相应方法清除缓存
 */
public interface CacheEvictService {

    /**
     * 清除所有字典缓存（科室、职称）
     * 在医生审核通过或医生信息变更时调用
     */
    public void evictDictCache();

    /**
     * 清除所有医生列表缓存（全量清除）
     * 在医生新增、审核通过、信息变更时调用
     */
    public void evictAllDoctorsCache();

    /**
     * 清除某个医生的所有排班缓存
     * 在排班新增、修改、删除，或预约创建/取消时调用
     */
    public void evictSchedulesByDoctor(Long doctorId);

    /**
     * 清除单个医生信息缓存
     */
    public void evictDoctorInfo(Long doctorId);

    /**
     * 医生信息变更时清除所有相关缓存
     */
    public void evictAllDoctorRelated(Long doctorId);

}
