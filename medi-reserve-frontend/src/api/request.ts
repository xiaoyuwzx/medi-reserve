import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/user'

// 定义后端统一响应结构（请根据实际情况调整）
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '', // 实际请求时会走 Vite 代理，所以 baseURL 留空
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动添加 JWT Token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    const token = userStore.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理 code 和错误
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, msg, data } = response.data
    if (code === 1) {
      // 成功，只返回 data 字段
      return data
    } else {
      // 业务错误，抛出自定义错误
      return Promise.reject(new Error(msg || '请求失败'))
    }
  },
  (error) => {
    // HTTP 状态码错误
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        // Token 失效，可清空 store 并跳转登录
        const userStore = useUserStore()
        userStore.clearToken()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(error.response.data?.msg || '请求异常'))
    }
    return Promise.reject(error)
  }
)

export default request