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