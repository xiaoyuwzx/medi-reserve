package com.medireserve.admin.service;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.dto.CertificateAuditDTO;
import com.medireserve.common.dto.DoctorPendingVO;
import com.medireserve.common.dto.PendingCertAuditVO;
import com.medireserve.common.entity.DoctorAudit;

/**
 * 管理员：医生账号审核
 */
public interface AdminAuditService {

    /**
     * 分页查询待审核医生列表
     * @param page
     * @param size
     * @return
     */
    PageInfo<DoctorPendingVO> listPending(int page, int size);

    /**
     * 查看医生审核详细
     * @param doctorId
     * @return
     */
    DoctorAudit getAuditDetail(Long doctorId);

    /**
     * 审核通过
     * @param doctorId
     * @param auditorId
     */
    void approve(Long doctorId, Long auditorId);

    /**
     * 审核驳回
     * @param doctorId
     * @param auditorId
     * @param rejectReason
     */
    void reject(Long doctorId, Long auditorId, String rejectReason);

    /**
     * 查询待审核证件列表（分页）
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<PendingCertAuditVO> listCertPending(int pageNum, int pageSize);

    /**
     * 查询待审核证件详情
     * @param doctorId
     * @return
     */
    PendingCertAuditVO getCertPendingDetail(Long doctorId);

    /**
     * 审核医生证件变更（通过/驳回）
     * @param doctorId
     * @param adminId
     * @param dto
     */
    void auditCertificate(Long doctorId, Long adminId, CertificateAuditDTO dto);
}
