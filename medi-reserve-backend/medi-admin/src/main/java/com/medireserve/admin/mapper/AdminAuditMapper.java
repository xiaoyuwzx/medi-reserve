package com.medireserve.admin.mapper;

import com.medireserve.common.dto.DoctorPendingVO;
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
    @Select("select d.id as doctorId, d.name, d.phone, d.department, d.title, d.created_at as createdAt, " +
            "da.specialty, da.introduction, da.certificate_url as certificateUrl, da.qualification_url as qualificationUrl " +
            "from doctor d inner join doctor_audit da on d.id = da.doctor_id " +
            "where da.audit_status = 0 order by d.created_at asc")
    List<DoctorPendingVO> findPendingList();

    /**
     * 根据医生ID查询医生数据
     * @param id
     * @return
     */
    @Select("select * from doctor where id = #{id}")
    Doctor findById(@Param("id") Long id);

    /**
     * 根据医生ID查询审核数据
     * @param doctorId
     * @return
     */
    @Select("select * from doctor_audit where doctor_id = #{doctorId}")
    DoctorAudit findByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 更新审核状态
     * @param doctorId
     * @param auditStatus
     * @param auditRemark
     * @param auditTime
     * @param auditorId
     * @return
     */
    @Update("update doctor_audit set " +
            "audit_status = #{auditStatus}, " +
            "audit_remark = #{auditRemark}, " +
            "audit_time = #{auditTime}, " +
            "auditor_id = #{auditorId} " +
            "where doctor_id = #{doctorId}")
    int updateAuditStatus(@Param("doctorId") Long doctorId,
                          @Param("auditStatus") Integer auditStatus,
                          @Param("auditRemark") String auditRemark,
                          @Param("auditTime") LocalDateTime auditTime,
                          @Param("auditorId") Long auditorId);
}
