package com.medireserve.doctor.mapper;

import com.medireserve.common.entity.DoctorAudit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

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
            "values (#{doctorId}, #{specialty}, #{introduction}, #{auditStatus});")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DoctorAudit doctorAudit);

}
