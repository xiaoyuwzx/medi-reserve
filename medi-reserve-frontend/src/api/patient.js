import request from './request'

/**
 * 患者端 API 接口封装
 */

// 患者登录
export function patientLogin(data) {
  return request({
    url: '/patient/login',
    method: 'post',
    data,
  })
}

// 患者注册
export function patientRegister(data) {
  return request({
    url: '/patient/register',
    method: 'post',
    data,
  })
}

// 获取科室列表
export function getDepartments() {
  return request({
    url: '/patient/departments',
    method: 'get',
  })
}

// 获取热门医生
export function getHotDoctors() {
  return request({
    url: '/patient/doctors/hot',
    method: 'get',
  })
}

// 获取医生列表（分页 + 筛选）
export function getDoctors(params) {
  return request({
    url: '/patient/doctors',
    method: 'get',
    params,
  })
}

// 获取医生排班日历
export function getDoctorSchedules(doctorId) {
  return request({
    url: `/patient/doctors/${doctorId}/schedules`,
    method: 'get',
  })
}

// 获取排班详情
export function getScheduleDetail(scheduleId) {
  return request({
    url: `/patient/schedules/${scheduleId}`,
    method: 'get',
  })
}

// 创建预约（下单）
export function createAppointment(scheduleId) {
  return request({
    url: '/patient/appointments',
    method: 'post',
    data: { scheduleId },
  })
}

// 模拟支付
export function payAppointment(appointmentId) {
  return request({
    url: `/patient/appointments/${appointmentId}/pay`,
    method: 'post',
  })
}

// 查询我的预约列表
export function getMyAppointments(params) {
  return request({
    url: '/patient/appointments',
    method: 'get',
    params,
  })
}

// 创建评价
export function createEvaluation(data) {
  return request({
    url: '/patient/evaluations',
    method: 'post',
    data,
  })
}

// 获取职称列表
export function getTitles() {
  return request({
    url: '/patient/dict/titles',
    method: 'get',
  })
}
