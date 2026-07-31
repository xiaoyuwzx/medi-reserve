package com.medireserve.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.admin.mapper.AdminPatientManageMapper;
import com.medireserve.admin.service.AdminPatientManageService;
import com.medireserve.common.dto.AdminPatientQueryDTO;
import com.medireserve.common.dto.AdminPatientStatusDTO;
import com.medireserve.common.dto.AdminPatientVO;
import com.medireserve.common.entity.Patient;
import com.medireserve.common.exception.AccountNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AdminPatientManageServiceImpl implements AdminPatientManageService {

    @Autowired
    private AdminPatientManageMapper adminPatientManageMapper;

    @Override
    public PageInfo<AdminPatientVO> listPatients(AdminPatientQueryDTO query) {
        log.info("管理员查询患者账号列表，条件：{}", query);

        PageHelper.startPage(query.getPage(), query.getSize());
        List<AdminPatientVO> list = adminPatientManageMapper.findPatientList(query);
        long total = adminPatientManageMapper.countPatientList(query);

        PageInfo<AdminPatientVO> pageInfo = new PageInfo<>(list);
        pageInfo.setTotal(total);

        return pageInfo;
    }

    @Override
    public AdminPatientVO getPatientDetail(Long patientId) {
        log.info("管理员查询患者详情，患者ID：{}", patientId);

        Patient patient = adminPatientManageMapper.findById(patientId);
        if (patient == null) {
            throw new AccountNotFoundException("患者不存在");
        }

        AdminPatientVO vo = new AdminPatientVO();
        BeanUtils.copyProperties(patient, vo);
        return vo;
    }

    @Override
    @Transactional
    public void updatePatientStatus(Long patientId, AdminPatientStatusDTO dto) {
        log.info("管理员修改患者账号状态，患者ID：{}，目标状态：{}", patientId, dto.getStatus());

        // 校验患者是否存在
        Patient patient = adminPatientManageMapper.findById(patientId);
        if (patient == null) {
            throw new AccountNotFoundException("患者不存在");
        }

        int rows = adminPatientManageMapper.updatePatientStatus(patientId, dto.getStatus());
        if (rows == 0) {
            throw new RuntimeException("修改患者状态失败");
        }

        log.info("患者账号状态修改成功，患者ID：{}，新状态：{}", patientId, dto.getStatus());
    }
}