import axios from 'axios'
import { applyInterceptors } from './request'

// 患者端代理前缀
export const patientInstance = axios.create({
  baseURL: '/api-patient',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})
applyInterceptors(patientInstance)

// 医生端代理前缀
export const doctorInstance = axios.create({
  baseURL: '/api-doctor',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})
applyInterceptors(doctorInstance)

// 管理端代理前缀
export const adminInstance = axios.create({
  baseURL: '/api-admin',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})
applyInterceptors(adminInstance)

// WebSocket 服务代理前缀（HTTP 部分）
export const websocketInstance = axios.create({
  baseURL: '/api-websocket',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})
applyInterceptors(websocketInstance)