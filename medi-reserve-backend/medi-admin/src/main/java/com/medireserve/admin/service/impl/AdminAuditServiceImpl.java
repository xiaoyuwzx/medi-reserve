package com.medireserve.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.admin.mapper.AdminAuditMapper;
import com.medireserve.admin.service.AdminAuditService;
import com.medireserve.common.constant.StatusConstant;
import com.medireserve.common.dto.DoctorPendingVO;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.DoctorAudit;
import com.medireserve.common.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员：医生账号审核
 */
@Slf4j
@Service
public class AdminAuditServiceImpl implements AdminAuditService {

    @Autowired
    private AdminAuditMapper adminAuditMapper;

    /**
     * 分页查询待审核医生列表
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<DoctorPendingVO> listPending(int page, int size) {

        log.info("查询待审核医生列表，页码：{}，每页：{}", page, size);

        //PageHelper.startPage() 会拦截下一条查询，自动添加 Limit 分页
        PageHelper.startPage(page, size);

        //执行查询
        List<DoctorPendingVO> list = adminAuditMapper.findPendingList();

        //封装分页数据 + 分页信息
        PageInfo<DoctorPendingVO> pageInfo = new PageInfo<>(list);

        log.info("查询完成，总记录数：{}", pageInfo.getTotal());

        return pageInfo;

    }

    /**
     * 查询医生审核详细
     * @param doctorId
     * @return
     */
    @Override
    public DoctorAudit getAuditDetail(Long doctorId) {

        log.info("查看医生审核详细，医生ID：{}", doctorId);

        //校验医生是否存在
        Doctor doctor = adminAuditMapper.findById(doctorId);
        if(doctor == null){
            log.warn("医生不存在，医生ID：{}", doctorId);
            throw new DoctorNotFoundException();
        }

        //查询审核数据
        DoctorAudit doctorAudit = adminAuditMapper.findByDoctorId(doctorId);
        if(doctorAudit == null){
            log.warn("医生审核数据不存在，医生ID：{}", doctorId);
            throw new DoctorAuditNotFoundException();
        }

        log.info("查询审核详细成功，医生ID：{}，审核状态：{}", doctorId, doctorAudit.getAuditStatus());

        return doctorAudit;

    }

    /**
     * 审核通过
     * @param doctorId
     * @param auditorId
     */
    @Override
    @Transactional
    public void approve(Long doctorId, Long auditorId) {

        log.info("审核通过，医生ID：{}，审核人：{}", doctorId, auditorId);

        //校验医生是否存在
        Doctor doctor = adminAuditMapper.findById(doctorId);
        if(doctor == null){
            log.warn("审核通过失败，医生不存在，医生ID：{}", doctorId);
            throw new DoctorNotFoundException();
        }

        //校验医生账号状态(账号被禁用不能通过审核)
        if(StatusConstant.ACCOUNT_DISABLED.equals(doctor.getStatus())){
            log.warn("审核通过失败，账号已禁用，医生ID：{}", doctorId);
            throw new AccountDisabledException("该医生账号已被禁用，无法通过审核");
        }

        //校验审核状态
        DoctorAudit doctorAudit = adminAuditMapper.findByDoctorId(doctorId);
        if (doctorAudit == null) {
            log.warn("审核通过失败，审核数据不存在，医生ID：{}", doctorId);
            throw new DoctorAuditNotFoundException();
        }
        if(!StatusConstant.AUDIT_PENDING.equals(doctorAudit.getAuditStatus())){
            log.warn("审核通过失败，医生已审核，当前状态：{}，医生ID：{}", doctorAudit.getAuditStatus(), doctorId);
            throw new DoctorAlreadyAuditedException();
        }

        //更新审核状态
        int rows = adminAuditMapper.updateAuditStatus(
                doctorId,
                StatusConstant.AUDIT_APPROVED, // 1
                null,        // 审核通过无驳回原因
                LocalDateTime.now(),
                auditorId
        );

        if(rows == 0){
            log.error("审核通过失败，更新数据库无影响，医生ID：{}", doctorId);
            throw new AuditOperationFailedException();
        }

        log.info("审核操作成功，医生ID：{}，审核人：{}", doctorId, auditorId);

    }

    /**
     * 审核驳回
     * @param doctorId
     * @param auditorId
     * @param rejectReason
     */
    @Override
    @Transactional
    public void reject(Long doctorId, Long auditorId, String rejectReason) {

        log.info("审核驳回，医生ID：{}，审核人：{}，驳回原因：{}", doctorId, auditorId, rejectReason);

        //校验驳回原因是否为空
        if(rejectReason == null ||rejectReason.trim().isEmpty()){
            log.warn("审核驳回失败，驳回原因为空，医生ID：{}", doctorId);
            throw new RejectReasonEmptyException();
        }

        //校验医生是否存在
        Doctor doctor = adminAuditMapper.findById(doctorId);
        if (doctor == null) {
            log.warn("审核驳回失败，医生不存在，医生ID：{}", doctorId);
            throw new DoctorNotFoundException();
        }

        //校验审核状态
        DoctorAudit doctorAudit = adminAuditMapper.findByDoctorId(doctorId);
        if (doctorAudit == null) {
            log.warn("审核驳回失败，审核数据不存在，医生ID：{}", doctorId);
            throw new DoctorAuditNotFoundException();
        }
        if(!StatusConstant.AUDIT_PENDING.equals(doctorAudit.getAuditStatus())){
            log.warn("审核驳回失败，医生已审核，当前状态：{}，医生ID：{}", doctorAudit.getAuditStatus(), doctorId);
            throw new DoctorAlreadyAuditedException();
        }

        //更新审核状态
        int rows = adminAuditMapper.updateAuditStatus(
                doctorId,
                StatusConstant.AUDIT_REJECTED, // 2
                rejectReason, //驳回原因
                LocalDateTime.now(),
                auditorId
        );

        if(rows == 0){
            log.error("审核驳回失败，更新数据库无影响，医生ID：{}", doctorId);
            throw new AuditOperationFailedException();
        }

        log.info("审核驳回成功，医生ID：{}，审核人：{}", doctorId, auditorId);

    }

}
