package com.medireserve.patient.service.impl;

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
import com.medireserve.patient.service.CacheEvictService;
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
    private CacheEvictService cacheEvictService;

    // ==================== 科室列表（使用 @Cacheable，但底层可改用多级缓存） ====================

    /**
     * 获取所有科室列表
     * 使用 Spring Cache 的 @Cacheable 注解，默认使用 Caffeine 缓存管理器
     */
    @Override
    @Cacheable(value = "departments")
    public List<DepartmentVO> getAllDepartments() {
        log.info("查询所有科室列表（回源）");
        return patientDoctorMapper.findAllDepartments();
    }

    // ==================== 职称列表 ====================

    @Override
    @Cacheable(value = "titles")
    public List<Title> getAllTitles() {
        log.info("查询所有职称列表（回源）");
        return titleMapper.findAll();
    }

    // ==================== 医生列表（分页，手动缓存） ====================

    /**
     * 分页查询医生列表
     * 使用多级缓存（Caffeine + Redis）
     *
     * 缓存Key = cache:doctors:{department}:{keyword}:{page}:{size}
     * 过期时间：5分钟（Redis），10分钟（Caffeine）
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

        // 使用多级缓存
        PageInfo<DoctorListVO> pageInfo = multiLevelCacheService.get(
                cacheKey,                    // Redis Key
                cacheKey,                    // 本地缓存Key（可以相同）
                () -> {                      // 回源函数
                    // 分页查询
                    PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());
                    List<DoctorListVO> list = patientDoctorMapper.findDoctorList(
                            queryDTO.getDepartment(),
                            queryDTO.getKeyword()
                    );
                    return new PageInfo<>(list);
                },
                300L  // Redis 过期时间：5分钟（300秒）
        );

        return pageInfo;
    }

    // ==================== 排班日历（布隆过滤器 + 多级缓存） ====================

    /**
     * 获取某医生未来7天的排班日历
     *
     * 防护机制：
     * 1. 先通过布隆过滤器判断医生ID是否存在，不存在则直接返回空列表
     * 2. 使用多级缓存（Caffeine + Redis）
     */
    @Override
    public List<ScheduleCalendarVO> getScheduleCalendar(Long doctorId) {
        // ========== 1. 布隆过滤器防穿透 ==========
        if (!bloomFilterService.mightContainDoctor(doctorId)) {
            log.warn("医生ID {} 不存在（布隆过滤器拦截）", doctorId);
            return List.of(); // 返回空列表
        }

        // ========== 2. 构建缓存Key ==========
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String cacheKey = CacheKeyConstants.buildSchedulesKey(doctorId, today);

        log.debug("查询排班日历，缓存Key: {}", cacheKey);

        // ========== 3. 多级缓存查询 ==========
        return multiLevelCacheService.get(
                cacheKey,
                cacheKey,
                () -> {
                    // ========== 4. 校验医生是否存在（二次校验） ==========
                    Doctor doctor = doctorAuthMapper.findById(doctorId);
                    if (doctor == null) {
                        log.warn("医生不存在，ID：{}", doctorId);
                        throw new DoctorNotFoundException();
                    }

                    // 校验审核状态
                    DoctorAudit audit = doctorAuditMapper.findByDoctorId(doctorId);
                    if (audit == null || !StatusConstant.AUDIT_APPROVED.equals(audit.getAuditStatus())) {
                        log.warn("医生未审核或审核未通过，ID：{}", doctorId);
                        throw new DoctorNotFoundException();
                    }

                    // 定义日期范围
                    LocalDate todayDate = LocalDate.now();
                    LocalDate endDate = todayDate.plusDays(DEFAULT_FUTURE_DAYS - 1);

                    // 查询排班日历
                    List<ScheduleCalendarVO> list = patientScheduleMapper.findSchedulesByDoctorIdAndDateRange(
                            doctorId, todayDate, endDate
                    );
                    log.info("排班日历回源查询，医生ID：{}，共 {} 条", doctorId, list.size());
                    return list;
                },
                60L  // Redis 过期时间：1分钟
        );
    }

    // ==================== 清除缓存 ====================

    /**
     * 清除某医生的排班缓存
     * 在预约创建/取消时调用
     */
    @Override
    public void clearScheduleCache(Long doctorId) {
        if (doctorId == null) return;
        // 使用统一缓存失效服务
        cacheEvictService.evictSchedulesByDoctor(doctorId);
        log.info("清除医生 {} 的排班缓存", doctorId);
    }
}