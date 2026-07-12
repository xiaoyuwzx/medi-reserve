package com.medireserve.patient.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import com.medireserve.patient.mapper.PatientDoctorMapper;
import com.medireserve.patient.mapper.PatientScheduleMapper;
import com.medireserve.patient.service.PatientDoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者端 - 医生/排班查询服务
 * 提供科室列表、医生列表、排班日历等查询功能
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

    /**
     * 获取所有科室列表(缓存一个小时减小数据库压力)
     * @return
     */
    @Override
    @Cacheable(value = "departments")
    public List<DepartmentVO> getAllDepartments() {

        log.info("查询所有科室列表");

        return patientDoctorMapper.findAllDepartments();

    }

    @Override
    @Cacheable(value = "titles")
    public List<Title> getAllTitles() {

        log.info("查询所有职称列表");

        return titleMapper.findAll();

    }

    /**
     * 分页查询医生列表(缓存5分钟)
     * @param doctorListQueryDTO
     * @return
     */
    @Override
    public PageInfo<DoctorListVO> getDoctorList(DoctorListQueryDTO doctorListQueryDTO) {

        log.info("查询医生列表，科室：{}，关键词：{}，页码：{}，每页：{}",
                doctorListQueryDTO.getDepartment(), doctorListQueryDTO.getKeyword(), doctorListQueryDTO.getPage(), doctorListQueryDTO.getSize());

        //使用PageHelper分页
        PageHelper.startPage(doctorListQueryDTO.getPage(), doctorListQueryDTO.getSize());

        //执行查询
        List<DoctorListVO> list = patientDoctorMapper.findDoctorList(
                doctorListQueryDTO.getDepartment(),
                doctorListQueryDTO.getKeyword()
        );

        //封装页面结果
        PageInfo<DoctorListVO> pageInfo = new PageInfo<>(list);

        log.info("查询完成，总记录数：{}，总页数：{}", pageInfo.getTotal(), pageInfo.getPages());

        return pageInfo;

    }

    /**
     * 获取某医生未来7天的排班日历(缓存1分钟)
     * @param doctorId
     * @return
     */
    @Override
    @Cacheable(value = "schedules", key = "#doctorId + '_' + T(java.time.LocalDate).now().format(T(java.time.format.DateTimeFormatter).ofPattern('yyyy-MM-dd'))")
    public List<ScheduleCalendarVO> getScheduleCalendar(Long doctorId) {

        log.info("查询医生排班日历，医生ID：{}", doctorId);

        //校验医生是否存在
        Doctor doctor = doctorAuthMapper.findById(doctorId);
        if(doctor == null){
            log.warn("医生不存在，医生ID：{}", doctorId);
            throw new DoctorNotFoundException();
        }

        // 校验审核状态
        DoctorAudit audit = doctorAuditMapper.findByDoctorId(doctorId);
        if (audit == null || !StatusConstant.AUDIT_APPROVED.equals(audit.getAuditStatus())) {
            log.warn("医生未审核或审核未通过，医生ID：{}", doctorId);
            throw new DoctorNotFoundException();  // 复用相同异常，避免透露具体原因
        }

        //定义日期范围
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(DEFAULT_FUTURE_DAYS - 1);

        log.info("查询日期范围：{} ~ {}", today, endDate);

        //查询排班日历
        List<ScheduleCalendarVO> calendarList = patientScheduleMapper.findSchedulesByDoctorIdAndDateRange(doctorId, today, endDate);

        log.info("查询完成，共 {} 条排班记录", calendarList.size());

        return calendarList;

    }

    /**
     * 清除某医生的排班缓存（预约创建/取消时调用）
     * @param doctorId
     */
    @Override
    @CacheEvict(value = "schedules", key = "#doctorId + '_' + T(java.time.LocalDate).now().format(T(java.time.format.DateTimeFormatter).ofPattern('yyyy-MM-dd'))")
    public void clearScheduleCache(Long doctorId) {

        log.info("清除医生排班缓存，医生ID：{}", doctorId);

    }
}
