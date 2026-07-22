package com.medireserve.common.mapper;

import com.medireserve.common.entity.DoctorAudit;
import org.apache.ibatis.annotations.*;

/**
 * 医生审核相关接口
 */
@Mapper
public interface DoctorAuditMapper {

    /**
     * 插入医生审核数据
     * @param doctorAudit
     * @return
     */
    @Insert("insert into doctor_audit (doctor_id, specialty, introduction, audit_status) " +
            "values (#{doctorId}, #{specialty}, #{introduction}, #{auditStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DoctorAudit doctorAudit);

    /**
     * 根据医生id查询审核数据
     * @param id
     * @return
     */
    @Select("SELECT * FROM doctor_audit WHERE doctor_id = #{doctorId}")
    DoctorAudit findByDoctorId(@Param("doctorId") Long id);

    /**
     * 更新医生审核资料（专业信息 + 提交证件审核）
     * 注意：普通信息立即生效，证件信息存入 pending 字段并设置状态为待审核
     */
    @Update("UPDATE doctor_audit " +
            "SET specialty = #{specialty}, introduction = #{introduction}, " +
            "pending_certificate_url = #{pendingCertificateUrl}, " +
            "pending_qualification_url = #{pendingQualificationUrl}, " +
            "cert_audit_status = 0, " +
            "cert_audit_remark = NULL, " +
            "cert_audit_time = NULL, " +
            "cert_auditor_id = NULL " +
            "WHERE doctor_id = #{doctorId}")
    int updateProfileAndSubmitCert(
            @Param("doctorId") Long doctorId,
            @Param("specialty") String specialty,
            @Param("introduction") String introduction,
            @Param("pendingCertificateUrl") String pendingCertificateUrl,
            @Param("pendingQualificationUrl") String pendingQualificationUrl
    );

    /**
     * 更新医生审核资料（专业信息）
     * @param doctorId
     * @param specialty
     * @param introduction
     * @return
     */
    @Update("UPDATE doctor_audit " +
            "SET specialty = #{specialty}, introduction = #{introduction} " +
            "WHERE doctor_id = #{doctorId}")
    int updateProfileOnly(@Param("doctorId") Long doctorId,
                          @Param("specialty") String specialty,
                          @Param("introduction") String introduction);

    /**
     * 审核通过：将 pending 字段覆盖到正式字段，清空 pending，状态置为 1
     */
    @Update("UPDATE doctor_audit " +
            "SET certificate_url = pending_certificate_url, " +
            "qualification_url = pending_qualification_url, " +
            "pending_certificate_url = NULL, " +
            "pending_qualification_url = NULL, " +
            "cert_audit_status = 1, " +
            "cert_audit_remark = #{remark}, " +
            "cert_audit_time = NOW(), " +
            "cert_auditor_id = #{auditorId} " +
            "WHERE doctor_id = #{doctorId} AND cert_audit_status = 0")
    int approveCert(@Param("doctorId") Long doctorId,
                    @Param("auditorId") Long auditorId,
                    @Param("remark") String remark);

    /**
     * 审核驳回：清空 pending，记录驳回原因，状态置为 2
     */
    @Update("UPDATE doctor_audit " +
            "SET pending_certificate_url = NULL, " +
            "pending_qualification_url = NULL, " +
            "cert_audit_status = 2, " +
            "cert_audit_remark = #{remark}, " +
            "cert_audit_time = NOW(), " +
            "cert_auditor_id = #{auditorId} " +
            "WHERE doctor_id = #{doctorId} AND cert_audit_status = 0")
    int rejectCert(@Param("doctorId") Long doctorId,
                   @Param("auditorId") Long auditorId,
                   @Param("remark") String remark);
}
