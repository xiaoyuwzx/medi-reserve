package com.medireserve.patient.mapper;

import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 预约挂号：挂号下单、支付、查询排班等
 */
@Mapper
public interface AppointmentMapper {

    /**
     * 根据排班ID查询排班信息
     * @param id
     * @return
     */
    @Select("select * from schedule where id = #{id}")
    Schedule findByScheduleId(Long id);

    /**
     * 根据医生ID查询医生信息
     * @param id
     * @return
     */
    @Select("select * from doctor where id = #{id}")
    Doctor findByDoctorId(Long id);

}
