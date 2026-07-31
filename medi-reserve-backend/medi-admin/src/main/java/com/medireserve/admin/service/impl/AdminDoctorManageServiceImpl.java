package com.medireserve.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.admin.mapper.AdminDoctorManageMapper;
import com.medireserve.admin.service.AdminDoctorManageService;
import com.medireserve.common.dto.AdminDoctorQueryDTO;
import com.medireserve.common.dto.AdminDoctorStatusDTO;
import com.medireserve.common.dto.AdminDoctorVO;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.exception.DoctorNotFoundException;
import com.medireserve.common.exception.PermissionDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AdminDoctorManageServiceImpl implements AdminDoctorManageService {

    @Autowired
    private AdminDoctorManageMapper adminDoctorManageMapper;

    @Override
    public PageInfo<AdminDoctorVO> listDoctors(AdminDoctorQueryDTO query) {
        log.info("管理员查询医生账号列表，条件：{}", query);

        PageHelper.startPage(query.getPage(), query.getSize());
        List<AdminDoctorVO> list = adminDoctorManageMapper.findDoctorList(query);
        long total = adminDoctorManageMapper.countDoctorList(query);

        PageInfo<AdminDoctorVO> pageInfo = new PageInfo<>(list);
        pageInfo.setTotal(total);

        return pageInfo;
    }

    @Override
    public AdminDoctorVO getDoctorDetail(Long doctorId) {
        log.info("管理员查询医生详情，医生ID：{}", doctorId);

        AdminDoctorVO detail = adminDoctorManageMapper.findDoctorDetail(doctorId);
        if (detail == null) {
            throw new DoctorNotFoundException("医生不存在");
        }

        return detail;
    }

    @Override
    @Transactional
    public void updateDoctorStatus(Long doctorId, AdminDoctorStatusDTO dto) {
        log.info("管理员修改医生账号状态，医生ID：{}，目标状态：{}", doctorId, dto.getStatus());

        // 校验医生是否存在
        Doctor doctor = adminDoctorManageMapper.findById(doctorId);
        if (doctor == null) {
            throw new DoctorNotFoundException("医生不存在");
        }

        int rows = adminDoctorManageMapper.updateDoctorStatus(doctorId, dto.getStatus());
        if (rows == 0) {
            throw new RuntimeException("修改医生状态失败");
        }

        log.info("医生账号状态修改成功，医生ID：{}，新状态：{}", doctorId, dto.getStatus());
    }
}