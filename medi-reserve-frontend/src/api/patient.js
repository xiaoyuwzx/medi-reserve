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
