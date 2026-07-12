package com.medireserve.patient.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.DepartmentVO;
import com.medireserve.common.dto.DoctorListQueryDTO;
import com.medireserve.common.dto.DoctorListVO;
import com.medireserve.doctor.mapper.DoctorAuthMapper;
import com.medireserve.patient.mapper.PatientDoctorMapper;
import com.medireserve.patient.mapper.PatientScheduleMapper;
import com.medireserve.patient.service.PatientDoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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

    /**
     * 获取所有科室列表(缓存一个小时减小数据库压力)
     * @return
     */
    @Override
    @Cacheable(value = "departments", unless = "#result == null || #result.isEmpty()")
    public List<DepartmentVO> getAllDepartments() {

        log.info("查询所有科室列表");

        return patientDoctorMapper.findAllDepartments();

    }

    /**
     * 分页查询医生列表(缓存5分钟)
     * @param doctorListQueryDTO
     * @return
     */
    @Override
    @Cacheable(value = "doctors", key = "#queryDTO.department + '_' + #queryDTO.keyword + '_' + #queryDTO.page + '_' + #queryDTO.size",
            unless = "#result == null")
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
}
