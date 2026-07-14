package com.medireserve.patient.mapper;

import com.medireserve.common.dto.DoctorHotVO;
import com.medireserve.common.dto.MyEvaluationVO;
import com.medireserve.common.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价数据访问层
 * 提供评价的增删改查操作
 */
@Mapper
public interface EvaluationMapper {

    /**
     * 根据预约ID查询评价数量
     * @param appointmentId
     * @return
     */
    @Select("select count(*) from evaluation where appointment_id = #{appointmentId}")
    int countByAppointmentId(@Param("appointmentId") Long appointmentId);

    /**
     * 插入评价记录
     * @param evaluation
     * @return
     */
    int insert(Evaluation evaluation);

    /**
     * 查询热门医生排行榜(使用时间衰减算法)
     * @param limit 返回条数默认为10
     * @return 返回医生列表
     */
    List<DoctorHotVO> findHotDoctors(@Param("limit") int limit);

    /**
     * 查询患者的所有评价(分页)
     * @param patientId
     * @return
     */
    List<MyEvaluationVO> findByPatientId(@Param("patientId") Long patientId);

}
