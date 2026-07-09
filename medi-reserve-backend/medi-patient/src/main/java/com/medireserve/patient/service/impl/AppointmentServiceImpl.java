package com.medireserve.patient.service.impl;

import com.medireserve.common.dto.ScheduleDetailVO;
import com.medireserve.common.entity.Doctor;
import com.medireserve.common.entity.Schedule;
import com.medireserve.common.exception.DoctorNotFoundException;
import com.medireserve.common.exception.ScheduleNotFoundException;
import com.medireserve.patient.mapper.AppointmentMapper;
import com.medireserve.patient.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预约挂号：挂号下单、支付、查询排班等
 */
@Slf4j
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    AppointmentMapper appointmentMapper;

    /**
     * 查询排班详细
     * @param scheduleId
     * @return
     */
    @Override
    public ScheduleDetailVO getScheduleDetail(Long scheduleId) {

        log.info("查询排班详细，排班ID：{}", scheduleId);

        //查询排班
        Schedule schedule = appointmentMapper.findByScheduleId(scheduleId);
        if(schedule == null){
            log.warn("排班不存在，排班ID：{}", scheduleId);
            throw new ScheduleNotFoundException();
        }

        //查询医生信息
        Doctor doctor = appointmentMapper.findByDoctorId(schedule.getDoctorId());
        if(doctor == null){
            log.warn("医生不存在，医生ID：{}", schedule.getDoctorId());
            throw new DoctorNotFoundException();
        }

        //组装返回对象
        ScheduleDetailVO scheduleDetailVO = new ScheduleDetailVO();
        scheduleDetailVO.setScheduleId(schedule.getId());
        scheduleDetailVO.setDoctorId(doctor.getId());
        scheduleDetailVO.setDoctorName(doctor.getName());
        scheduleDetailVO.setDepartment(doctor.getDepartment());
        scheduleDetailVO.setTitle(doctor.getTitle());
        scheduleDetailVO.setScheduleDate(schedule.getScheduleDate());
        scheduleDetailVO.setPeriod(schedule.getPeriod());
        scheduleDetailVO.setPeriodText(schedule.getPeriod() == 1 ? "上午" : "下午");
        scheduleDetailVO.setRemainingCount(schedule.getRemainingCount());
        scheduleDetailVO.setStatus(schedule.getStatus());
        //文本状态
        String statusText;
        if(schedule.getStatus() == 1){
            statusText = "正常";
        }else if(schedule.getStatus() == 2){
            statusText = "已停诊";
        }else if(schedule.getStatus() == 3){
            statusText = "已满";
        }else{
            statusText = "未知";
        }
        scheduleDetailVO.setStatusText(statusText);

        log.info("排班详细查询成功，排班ID：{}", scheduleId);

        return scheduleDetailVO;

    }

}
