package com.medireserve.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.medireserve.admin.mapper.AdminAuditMapper;
import com.medireserve.common.dto.CertificateAuditDTO;
import com.medireserve.common.dto.PendingCertAuditVO;
import com.medireserve.common.mapper.DoctorAuditMapper;
import com.medireserve.common.mapper.DoctorAuthMapper;
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
import org.springframework.util.StringUtils;

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

    @Autowired
    private DoctorAuthMapper doctorAuthMapper;

    @Autowired
    private DoctorAuditMapper doctorAuditMapper;


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
        Doctor doctor = doctorAuthMapper.findById(doctorId);
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
        Doctor doctor = doctorAuthMapper.findById(doctorId);
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
        Doctor doctor = doctorAuthMapper.findById(doctorId);
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
                auditorId
        );

        if(rows == 0){
            log.error("审核驳回失败，更新数据库无影响，医生ID：{}", doctorId);
            throw new AuditOperationFailedException();
        }

        log.info("审核驳回成功，医生ID：{}，审核人：{}", doctorId, auditorId);

    }

    /**
     * 查询待审核证件列表（分页）
     */
    @Override
    public PageInfo<PendingCertAuditVO> listCertPending(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PendingCertAuditVO> list = adminAuditMapper.findCertPendingList();
        return new PageInfo<>(list);
    }

    /**
     * 查询待审核证件详情
     */
    @Override
    public PendingCertAuditVO getCertPendingDetail(Long doctorId) {
        // 先验证医生是否存在
        if (doctorAuthMapper.findById(doctorId) == null) {
            throw new DoctorNotFoundException();
        }

        PendingCertAuditVO vo = adminAuditMapper.findCertPendingByDoctorId(doctorId);
        if (vo == null) {
            throw new BusinessException("该医生未提交证件变更申请");
        }
        return vo;
    }

    /**
     * 审核医生证件变更（通过/驳回）
     */
    @Transactional
    @Override
    public void auditCertificate(Long doctorId, Long adminId, CertificateAuditDTO dto) {
        // 1. 验证医生是否存在
        if (doctorAuthMapper.findById(doctorId) == null) {
            throw new DoctorNotFoundException();
        }

        // 2. 查询当前审核状态
        DoctorAudit audit = adminAuditMapper.findByDoctorId(doctorId);
        if (audit == null) {
            throw new DoctorAuditNotFoundException();
        }

        // 3. 验证是否有待审核的证件
        if (audit.getCertAuditStatus() == null || audit.getCertAuditStatus() != 0) {
            throw new BusinessException("该医生没有待审核的证件变更");
        }
        if (!StringUtils.hasText(audit.getPendingCertificateUrl())
                && !StringUtils.hasText(audit.getPendingQualificationUrl())) {
            throw new BusinessException("该医生没有待审核的证件变更");
        }

        // 4. 执行审核
        int rows;
        if (dto.getResult() == 1) {
            // 审核通过：pending 覆盖到正式字段
            rows = doctorAuditMapper.approveCert(doctorId, adminId, dto.getRemark());
            log.info("证件审核通过，医生ID：{}，管理员ID：{}", doctorId, adminId);
        } else if (dto.getResult() == 2) {
            // 审核驳回：需要填写驳回原因
            if (!StringUtils.hasText(dto.getRemark())) {
                throw new BusinessException("驳回时必须填写驳回原因");
            }
            rows = doctorAuditMapper.rejectCert(doctorId, adminId, dto.getRemark());
            log.info("证件审核驳回，医生ID：{}，管理员ID：{}，原因：{}",
                    doctorId, adminId, dto.getRemark());
        } else {
            throw new BusinessException("审核结果参数错误，请传入 1（通过）或 2（驳回）");
        }

        if (rows == 0) {
            throw new AuditOperationFailedException("审核操作失败，请重试");
        }
    }

}
