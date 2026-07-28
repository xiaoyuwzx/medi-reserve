import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/user'

export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

/**
 * 为指定的 axios 实例添加统一的拦截器
 * @param instance 需要添加拦截器的 axios 实例
 */
export function applyInterceptors(instance: AxiosInstance): void {
  // 请求拦截器：自动添加 JWT Token
  instance.interceptors.request.use(
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
  instance.interceptors.response.use(
    (response: AxiosResponse<ApiResponse>) => {
      const { code, msg, data } = response.data
      if (code === 1) {
        return data
      } else {
        return Promise.reject(new Error(msg || '请求失败'))
      }
    },
    (error) => {
      if (error.response) {
        const status = error.response.status
        if (status === 401) {
          const userStore = useUserStore()
          userStore.clearToken()
          window.location.href = '/login'
        }
        return Promise.reject(new Error(error.response.data?.msg || '请求异常'))
      }
      return Promise.reject(error)
    }
  )
}