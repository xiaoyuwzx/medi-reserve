import request from './request'

/**
 * 医生端 API 接口封装
 */

// 医生登录
export function doctorLogin(data) {
  return request({
    url: '/doctor/login',
    method: 'post',
    data,
  })
}

// 医生注册
export function doctorRegister(data) {
  return request({
    url: '/doctor/register',
    method: 'post',
    data,
  })
}

// 获取推荐号源数
export function getRecommendedMaxCount(scheduleDate, userInputMax = 20) {
  return request({
    url: '/doctor/schedules/recommend',
    method: 'get',
    params: { scheduleDate, userInputMax },
  })
}

// 新增排班
export function createSchedule(data) {
  return request({
    url: '/doctor/schedules',
    method: 'post',
    data,
  })
}

// 查询排班列表
export function getSchedules(params) {
  return request({
    url: '/doctor/schedules',
    method: 'get',
    params,
  })
}

// 修改排班状态（停诊/恢复）
export function updateScheduleStatus(scheduleId, status) {
  return request({
    url: `/doctor/schedules/${scheduleId}/status`,
    method: 'patch',
    params: { status },
  })
}

// 删除排班
export function deleteSchedule(scheduleId) {
  return request({
    url: `/doctor/schedules/${scheduleId}`,
    method: 'delete',
  })
}

// 获取医生问诊预约列表
export function getDoctorAppointments(params) {
  return request({
    url: '/doctor/appointments',
    method: 'get',
    params,
  })
}

// 获取问诊室信息
export function getConsultationRoom(appointmentId) {
  return request({
    url: `/consultation/room/${appointmentId}`,
    method: 'get',
  })
}

// 获取聊天历史
export function getChatHistory(appointmentId, params) {
  return request({
    url: `/consultation/history/${appointmentId}`,
    method: 'get',
    params,
  })
}

// 结束问诊
export function endConsultation(appointmentId) {
  return request({
    url: `/consultation/end/${appointmentId}`,
    method: 'post',
  })
}

// 获取 OSS STS 临时凭证
export function getStsToken() {
  return request({
    url: '/doctor/oss/sts',
    method: 'get',
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/doctor/password',
    method: 'put',
    data,
  })
}
