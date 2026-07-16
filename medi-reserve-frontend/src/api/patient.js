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