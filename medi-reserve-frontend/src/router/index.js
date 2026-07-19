import { createRouter, createWebHashHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { jwtDecode } from 'jwt-decode'

// 定义路由
const routes = [
  // ==================== 统一登录 ====================
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  // 旧登录路径重定向
  { path: '/patient/login', redirect: '/login' },

  // ==================== 患者端 ====================
  {
    path: '/patient/register',
    name: 'PatientRegister',
    component: () => import('@/views/patient/Register.vue'),
    meta: { title: '患者注册', requiresAuth: false },
  },
  {
    path: '/patient',
    component: () => import('@/components/Layout/index.vue'),
    meta: { requiresAuth: true, role: 'PATIENT' },
    children: [
      {
        path: 'home',
        name: 'PatientHome',
        component: () => import('@/views/patient/Home.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'doctors',
        name: 'PatientDoctors',
        component: () => import('@/views/patient/Doctors.vue'),
        meta: { title: '找医生' },
      },
      {
        path: 'schedule/:doctorId',
        name: 'PatientSchedule',
        component: () => import('@/views/patient/Schedule.vue'),
        meta: { title: '排班日历' },
      },
      {
        path: 'confirm/:scheduleId',
        name: 'PatientConfirm',
        component: () => import('@/views/patient/Confirm.vue'),
        meta: { title: '确认预约' },
      },
      {
        path: 'pay/:appointmentId',
        name: 'PatientPay',
        component: () => import('@/views/patient/Pay.vue'),
        meta: { title: '支付' },
      },
      {
        path: 'orders',
        name: 'PatientOrders',
        component: () => import('@/views/patient/Orders.vue'),
        meta: { title: '我的预约' },
      },
      {
        path: 'evaluate/:appointmentId',
        name: 'PatientEvaluate',
        component: () => import('@/views/patient/Evaluate.vue'),
        meta: { title: '就诊评价' },
      },
      {
        path: 'hot',
        name: 'PatientHot',
        component: () => import('@/views/patient/HotDoctors.vue'),
        meta: { title: '热门医生' },
      },
      {
        path: 'chat/:appointmentId?',
        name: 'PatientChat',
        component: () => import('@/views/patient/ChatRoom.vue'),
        meta: { title: '在线问诊' },
      },
      {
        path: 'profile',
        name: 'PatientProfile',
        component: () => import('@/views/patient/Profile.vue'),
        meta: { title: '个人信息' },
      },
    ],
  },

  // ==================== 医生端 ====================
  { path: '/doctor/login', redirect: '/login?role=doctor' },
  {
    path: '/doctor/register',
    name: 'DoctorRegister',
    component: () => import('@/views/doctor/Register.vue'),
    meta: { title: '医生注册', requiresAuth: false },
  },
  {
    path: '/doctor',
    component: () => import('@/components/Layout/index.vue'),
    meta: { requiresAuth: true, role: 'DOCTOR' },
    children: [
      {
        path: 'schedules',
        name: 'DoctorSchedules',
        component: () => import('@/views/doctor/ScheduleManage.vue'),
        meta: { title: '排班管理' },
      },
      {
        path: 'chat/:appointmentId?',
        name: 'DoctorChat',
        component: () => import('@/views/doctor/ChatRoom.vue'),
        meta: { title: '在线问诊' },
      },
    ],
  },

  // ==================== 管理端 ====================
  { path: '/admin/login', redirect: '/login?role=admin' },
  {
    path: '/admin',
    component: () => import('@/components/Layout/index.vue'),
    meta: { requiresAuth: true, role: 'SUPER_ADMIN' },
    children: [
      {
        path: 'audit',
        name: 'AdminAudit',
        component: () => import('@/views/admin/Audit.vue'),
        meta: { title: '医生审核' },
      },
      {
        path: 'admins',
        name: 'AdminManage',
        component: () => import('@/views/admin/AdminManage.vue'),
        meta: { title: '管理员管理' },
      },
    ],
  },

  // 默认重定向
  { path: '/', redirect: '/patient/home' },
  { path: '/:pathMatch(.*)*', redirect: '/patient/home' },
]

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes,
})

// -------- 工具函数 --------

// 验证 Token 是否过期
function isTokenExpired(token) {
  if (!token) return true
  try {
    const decoded = jwtDecode(token)
    // JWT exp 单位是秒，Date.now() 单位是毫秒
    const currentTime = Date.now() / 1000
    return decoded.exp < currentTime
  } catch {
    // 解析失败，视为无效 Token
    return true
  }
}

// 跳转到对应登录页
function redirectToLogin(to, next) {
  if (to.path.startsWith('/admin')) {
    next('/admin/login')
  } else {
    // 患者端和医生端统一跳转到 /login
    next('/login')
  }
}

// -------- 路由守卫 --------
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null
  const role = userInfo?.role || null

  // 1. 需要登录但未登录
  if (to.meta.requiresAuth && !token) {
    redirectToLogin(to, next)
    return
  }

  // 2. 需要登录 + 有 Token 但 Token 已过期
  if (to.meta.requiresAuth && token && isTokenExpired(token)) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.warning('登录已过期，请重新登录')
    redirectToLogin(to, next)
    return
  }

  // 3. 需要角色校验
  if (to.meta.requiresAuth && to.meta.role) {
    if (role !== to.meta.role) {
      // 角色不匹配，跳转到对应首页
      if (role === 'PATIENT') next('/patient/home')
      else if (role === 'DOCTOR') next('/doctor/schedules')
      else if (role === 'SUPER_ADMIN') next('/admin/audit')
      else next('/login')
      return
    }
  }

  // 4. 已登录但访问登录页
  if (token && !to.meta.requiresAuth) {
    // 管理员登录页：清除当前 token，允许重新登录
    if (to.path === '/login' && to.query.role === 'admin') {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      next()
      return
    }
    if (to.path === '/login' || to.path === '/patient/register') {
      if (role === 'PATIENT') next('/patient/home')
      else if (role === 'DOCTOR') next('/doctor/schedules')
      else next('/patient/home')
    } else if (to.path === '/doctor/register') {
      next('/doctor/schedules')
    } else if (to.path === '/admin/login') {
      next('/admin/audit')
    } else {
      next()
    }
    return
  }

  next()
})

export default router