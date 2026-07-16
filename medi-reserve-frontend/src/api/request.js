import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '', // Vite 代理自动转发 /patient、/doctor、/admin
  timeout: 15000,
})

// -------- 请求拦截器 --------
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// -------- 响应拦截器 --------
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端 code === 1 表示成功
    if (res.code === 1) {
      return res
    }
    // 业务错误（如密码错误、账号不存在等）
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    // HTTP 错误处理
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        // Token 无效或未登录，跳转登录页
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        const msg = data?.msg || '登录已过期，请重新登录'
        ElMessage.error(msg)
        router.push('/patient/login')
      } else if (status === 403) {
        ElMessage.error(data?.msg || '没有权限执行此操作')
      } else {
        ElMessage.error(data?.msg || `请求错误（${status}）`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络')
    } else {
      ElMessage.error('网络异常，请稍后重试')
    }
    return Promise.reject(error)
  },
)

export default request