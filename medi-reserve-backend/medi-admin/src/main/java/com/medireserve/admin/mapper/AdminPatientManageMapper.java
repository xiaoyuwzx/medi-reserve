package com.medireserve.admin.mapper;

import com.medireserve.common.dto.AdminPatientQueryDTO;
import com.medireserve.common.dto.AdminPatientVO;
import com.medireserve.common.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 管理员端 - 患者账号管理 Mapper
 */
@Mapper
public interface AdminPatientManageMapper {

    /**
     * 分页查询患者账号列表（支持关键词、状态筛选）
     * @param query 查询条件
     * @return 患者列表
     */
    List<AdminPatientVO> findPatientList(@Param("q") AdminPatientQueryDTO query);

    /**
     * 统计符合条件的患者总数
     */
    long countPatientList(@Param("q") AdminPatientQueryDTO query);

    /**
     * 根据ID查询患者详情
     */
    @Select("SELECT * FROM patient WHERE id = #{patientId}")
    Patient findById(@Param("patientId") Long patientId);

    /**
     * 更新患者账号状态
     */
    @Update("UPDATE patient SET status = #{status} WHERE id = #{id}")
    int updatePatientStatus(@Param("id") Long id, @Param("status") Integer status);
}