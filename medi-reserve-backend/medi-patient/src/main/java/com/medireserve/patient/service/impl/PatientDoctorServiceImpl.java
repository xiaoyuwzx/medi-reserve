package com.medireserve.patient.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.common.constant.CacheKeyConstants;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.DepartmentVO;
import com.medireserve.common.dto.DoctorListQueryDTO;
import com.medireserve.common.dto.DoctorListVO;
import com.medireserve.common.dto.ScheduleCalendarVO;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.DoctorAudit;
import com.medireserve.common.entity.Title;
import com.medireserve.common.exception.DoctorNotFoundException;
import com.medireserve.common.mapper.DoctorAuditMapper;
import com.medireserve.common.mapper.DoctorAuthMapper;
import com.medireserve.common.mapper.TitleMapper;
import com.medireserve.common.service.BloomFilterService;
import com.medireserve.common.service.MultiLevelCacheService;
import com.medireserve.patient.mapper.PatientDoctorMapper;
import com.medireserve.patient.mapper.PatientScheduleMapper;
import com.medireserve.patient.service.PatientDoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 患者端 - 医生/排班查询服务
 * 集成多级缓存（Caffeine + Redis + 布隆过滤器）
 * 核心职责：
 * 1. 科室列表、职称列表（使用 @Cacheable 注解，走 Spring Cache + Redis）
 * 2. 医生列表分页查询（使用 MultiLevelCacheService，Caffeine + Redis + 布隆过滤器三层防护）
 * 3. 排班日历查询（布隆过滤器 + MultiLevelCacheService 双层防护）
 * 4. 缓存失效清除（委托给 CacheEvictServiceImpl）
 *
 * 缓存策略：
 * - 科室/职称：Spring @Cacheable，Redis 缓存 1 小时（变化极少）
 * - 医生列表：自定义多级缓存，Redis TTL 300 秒，Caffeine 10 分钟
 * - 排班日历：自定义多级缓存，Redis TTL 60 秒（变化频繁）
 * - 布隆过滤器：拦截不存在的医生 ID，防止缓存穿透
 */
@Slf4j
@Service
public class PatientDoctorServiceImpl implements PatientDoctorService {

    /** 排班日历默认查询未来天数 */
    private static final int DEFAULT_FUTURE_DAYS = 7;

    @Autowired
    private DoctorAuthMapper doctorAuthMapper;

    @Autowired
    private PatientDoctorMapper patientDoctorMapper;

    @Autowired
    private PatientScheduleMapper patientScheduleMapper;

    @Autowired
    private TitleMapper titleMapper;

    @Autowired
    private DoctorAuditMapper doctorAuditMapper;

    @Autowired
    private MultiLevelCacheService multiLevelCacheService;

    @Autowired
    private BloomFilterService bloomFilterService;

    @Autowired
    private CacheEvictServiceImpl cacheEvictServiceImpl;

    // ==================== 科室列表（使用 @Cacheable） ====================

    /**
     * 获取所有科室列表（含各科室医生数量）
     *
     * 缓存策略：使用 @Cacheable 注解
     * - 缓存名：departments
     * - 存储介质：Redis（由 CacheConfig 配置）
     * - 过期时间：1 小时（科室列表几乎不变）
     *
     * 注意：每次查询时，如果缓存未命中，会回源到 DB 查询
     *
     * @return 科室列表（每个科室包含 ID、名称、医生数量）
     */
    @Override
    @Cacheable(value = "departments")
    public List<DepartmentVO> getAllDepartments() {
        log.info("查询所有科室列表（回源）");
        return patientDoctorMapper.findAllDepartments();
    }

    // ==================== 职称列表 ====================

    /**
     * 获取所有职称列表
     *
     * 缓存策略：使用 @Cacheable 注解
     * - 缓存名：titles
     * - 存储介质：Redis（由 CacheConfig 配置）
     * - 过期时间：1 小时（职称列表几乎不变）
     *
     * @return 职称列表
     */
    @Override
    @Cacheable(value = "titles")
    public List<Title> getAllTitles() {
        log.info("查询所有职称列表（回源）");
        return titleMapper.findAll();
    }

    // ==================== 医生列表（多级缓存 + TypeReference） ====================

    /**
     * 分页查询医生列表（支持科室筛选、关键词搜索）
     * 使用多级缓存（Caffeine + Redis），通过 TypeReference 解决 PageInfo 泛型反序列化问题
     *
     * 缓存策略：多级缓存（Caffeine + Redis）
     * - 缓存 Key 包含：科室、关键词、页码、每页大小
     * - Redis TTL：300 秒（5 分钟）
     * - Caffeine TTL：10 分钟（由 MultiLevelCacheService 统一管理）
     *
     * 为什么不用 @Cacheable？
     * - 分页查询的缓存 Key 需要动态拼接（包含科室、关键词、页码等）
     * - 且需要支持泛型反序列化（PageInfo<DoctorListVO>）
     * - 使用 MultiLevelCacheService 更灵活
     *
     * 泛型处理：
     * - 使用 TypeReference 捕获 PageInfo<DoctorListVO> 的类型信息
     * - 确保从 Redis 读取时能正确反序列化为 PageInfo<DoctorListVO>，而非 LinkedHashMap
     *
     * @param queryDTO 查询参数（科室、关键词、页码、每页大小）
     * @return 分页结果（PageInfo<DoctorListVO>）
     */
    @Override
    public PageInfo<DoctorListVO> getDoctorList(DoctorListQueryDTO queryDTO) {
        // 构建缓存Key
        String cacheKey = CacheKeyConstants.buildDoctorsKey(
                queryDTO.getDepartment(),
                queryDTO.getKeyword(),
                queryDTO.getPage(),
                queryDTO.getSize()
        );

        log.debug("查询医生列表，缓存Key: {}", cacheKey);

        // 使用多级缓存，传递 Type 确保 PageInfo 泛型正确反序列化
        PageInfo<DoctorListVO> pageInfo = multiLevelCacheService.get(
                cacheKey,       // Redis Key
                cacheKey,       // Caffeine Key
                () -> {         // 回源函数
                    PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
                    List<DoctorListVO> list = patientDoctorMapper.findDoctorList(
                            queryDTO.getDepartment(),
                            queryDTO.getKeyword()
                    );
                    return new PageInfo<>(list);
                },
                300L,       // Redis TTL 300 秒
                new TypeReference<PageInfo<DoctorListVO>>() {}.getType()    // 泛型类型
        );

        return pageInfo;
    }

    // ==================== 排班日历（布隆过滤器 + 多级缓存） ====================

    /**
     * 获取某医生未来 7 天的排班日历
     *
     * 缓存策略：布隆过滤器 + 多级缓存
     *
     * 三层防护：
     * 1. 布隆过滤器：拦截不存在的医生 ID，避免查询穿透到 DB
     * 2. 多级缓存（L1 Caffeine + L2 Redis）
     * 3. 回源查询（最终校验医生是否存在 + 审核状态 + 查询排班）
     *
     * 缓存 Key 格式：cache:schedules:{doctorId}:{today}
     * - 例如：cache:schedules:123:2026-07-25
     * - 按日期分片，保证每天的缓存独立
     *
     * Redis TTL：60 秒（排班变化较频繁，短 TTL 保证数据实时性）
     *
     * @param doctorId 医生 ID
     * @return 未来 7 天的排班列表
     */
    @Override
    public List<ScheduleCalendarVO> getScheduleCalendar(Long doctorId) {
        // 1. 布隆过滤器防穿透（拦截不存在的医生）
        if (!bloomFilterService.mightContainDoctor(doctorId)) {
            log.warn("医生ID {} 不存在（布隆过滤器拦截）", doctorId);
            return List.of();
        }

        // 2. 构建缓存Key（多级缓存）
        // 以当天日期作为 Key 的一部分，确保每天的缓存独立
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String cacheKey = CacheKeyConstants.buildSchedulesKey(doctorId, today);

        log.debug("查询排班日历，缓存Key: {}", cacheKey);

        // 3. 多级缓存查询，使用 TypeReference 保留泛型
        return multiLevelCacheService.get(
                cacheKey,
                cacheKey,
                () -> {
                    // 校验医生是否存在
                    // 回源函数：布隆过滤器说可能存在，但最终以 DB 为准
                    Doctor doctor = doctorAuthMapper.findById(doctorId);
                    if (doctor == null) {
                        // 如果 DB 里真的没有（布隆过滤器误判了），抛异常
                        log.warn("医生不存在，ID：{}", doctorId);
                        throw new DoctorNotFoundException();
                    }

                    // 校验审核状态
                    DoctorAudit audit = doctorAuditMapper.findByDoctorId(doctorId);
                    if (audit == null || !StatusConstant.AUDIT_APPROVED.equals(audit.getAuditStatus())) {
                        log.warn("医生未审核或审核未通过，ID：{}", doctorId);
                        throw new DoctorNotFoundException();
                    }

                    // 定义日期范围：今天 ~ 今天 + 6 天（共 7 天）
                    LocalDate todayDate = LocalDate.now();
                    LocalDate endDate = todayDate.plusDays(DEFAULT_FUTURE_DAYS - 1);

                    // 查询排班日历
                    List<ScheduleCalendarVO> list = patientScheduleMapper.findSchedulesByDoctorIdAndDateRange(
                            doctorId, todayDate, endDate
                    );
                    log.info("排班日历回源查询，医生ID：{}，共 {} 条", doctorId, list.size());
                    return list;
                },
                60L,    // Redis 缓存 60 秒
                new TypeReference<List<ScheduleCalendarVO>>() {}.getType()
        );
    }

    // ==================== 清除缓存 ====================

    /**
     * 清除某医生的排班缓存
     *
     * 使用场景：
     * - 患者创建预约后：剩余号源发生变化，需要清除缓存
     * - 超时取消预约后：号源回滚，需要清除缓存
     * - 医生停诊/恢复排班后：排班状态变化，需要清除缓存
     *
     * 实现方式：
     * - 委托给 CacheEvictServiceImpl.evictSchedulesByDoctor
     * - 该方法会按模式 cache:schedules:{doctorId}:* 清除所有日期的缓存
     *
     * 注意：清除的是该医生所有日期的排班缓存，而非当天
     * 因为未来 7 天中任何一天的变化，都需要让患者看到最新状态
     *
     * @param doctorId 医生 ID
     */
    @Override
    public void clearScheduleCache(Long doctorId) {
        if (doctorId == null) return;
        cacheEvictServiceImpl.evictSchedulesByDoctor(doctorId);
        log.info("清除医生 {} 的排班缓存", doctorId);
    }
}