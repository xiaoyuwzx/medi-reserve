package com.medireserve.admin.mapper;

import com.medireserve.common.dto.DoctorPendingVO;
import com.medireserve.common.dto.PendingCertAuditVO;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.DoctorAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminAuditMapper {

    /**
     * 分页查询待审核医生列表
     * @return
     */
    List<DoctorPendingVO> findPendingList();

    /**
     * 统计待审核医生数量
     */
    long countPending();

    /**
     * 根据医生ID查询审核资料
     */
    @Select("SELECT * FROM doctor_audit WHERE doctor_id = #{doctorId}")
    DoctorAudit findByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 更新审核状态（首次注册审核）
     */
    int updateAuditStatus(@Param("doctorId") Long doctorId,
                          @Param("auditStatus") Integer auditStatus,
                          @Param("auditRemark") String auditRemark,
                          @Param("auditorId") Long auditorId);

    /**
     * 查询待审核证件列表（证件变更审核）
     */
    List<PendingCertAuditVO> findCertPendingList();

    /**
     * 统计待审核证件数量
     */
    long countCertPending();

    /**
     * 根据医生ID查询待审核证件信息
     */
    PendingCertAuditVO findCertPendingByDoctorId(@Param("doctorId") Long doctorId);
}
