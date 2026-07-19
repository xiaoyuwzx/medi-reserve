import request from './request'

/**
 * 管理端 API 接口封装
 */

// 管理员登录
export function adminLogin(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data,
  })
}

// 获取待审核医生列表
export function getPendingDoctors(params) {
  return request({
    url: '/admin/doctors/pending',
    method: 'get',
    params,
  })
}

// 获取医生审核详情
export function getAuditDetail(doctorId) {
  return request({
    url: `/admin/doctors/${doctorId}/audit-detail`,
    method: 'get',
  })
}

// 审核通过
export function approveDoctor(doctorId) {
  return request({
    url: `/admin/doctors/${doctorId}/approve`,
    method: 'patch',
  })
}

// 审核驳回
export function rejectDoctor(doctorId, data) {
  return request({
    url: `/admin/doctors/${doctorId}/reject`,
    method: 'patch',
    data,
  })
}

// 获取管理员列表
export function getAdminList(params) {
  return request({
    url: '/admin/list',
    method: 'get',
    params,
  })
}

// 修改管理员状态（禁用/启用）
export function updateAdminStatus(adminId, status) {
  return request({
    url: `/admin/${adminId}/status`,
    method: 'patch',
    data: { status },
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/admin/password',
    method: 'put',
    data,
  })
}
