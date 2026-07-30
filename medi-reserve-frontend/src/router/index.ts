import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Login from '@/views/patient/Login.vue'
import Register from '@/views/patient/Register.vue'
import Home from '@/views/patient/Home.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/patient/login',
      name: 'PatientLogin',
      component: Login
    },
    {
      path: '/patient/register',
      name: 'PatientRegister',
      component: Register
    },
    {
      path: '/patient',
      name: 'PatientHome',
      component: Home
    },
    {
      path: '/patient/doctors',
      name: 'DoctorList',
      component: () => import('@/views/patient/DoctorList.vue'),
      meta: { title: '医生列表' }
    },
    {
      path: '/patient/doctor/:doctorId',
      name: 'DoctorDetail',
      component: () => import('@/views/patient/DoctorDetail.vue'),
      meta: { title: '医生详情' }
    },
    {
      path: '/patient/appointment/:scheduleId',
      name: 'AppointmentConfirm',
      component: () => import('@/views/patient/AppointmentConfirm.vue'),
      meta: { title: '确认挂号' }
    },
    {
      path: '/patient/payment',
      name: 'PaymentPage',
      component: () => import('@/views/patient/PaymentPage.vue'),
      meta: { title: '确认支付' }
    },
    {
      path: '/patient/payment/result',
      name: 'PaymentResult',
      component: () => import('@/views/patient/PaymentResult.vue'),
      meta: { title: '支付结果' }
    },
    {
      path: '/patient/appointments',
      name: 'MyAppointments',
      component: () => import('@/views/patient/MyAppointments.vue'),
      meta: { title: '我的预约' }
    },
    {
      path: '/patient/evaluation/create/:appointmentId',
      name: 'EvaluationCreate',
      component: () => import('@/views/patient/EvaluationCreate.vue'),
      meta: { title: '评价医生' }
    },
    {
      path: '/patient/my-evaluations',
      name: 'MyEvaluations',
      component: () => import('@/views/patient/MyEvaluations.vue'),
      meta: { title: '我的评价' }
    },
    {
      path: '/patient/profile',
      name: 'PatientProfile',
      component: () => import('@/views/patient/Profile.vue'),
      meta: { title: '个人信息' }
    },
    {
      path: '/patient/password',
      name: 'PatientPassword',
      component: () => import('@/views/patient/Password.vue'),
      meta: { title: '修改密码' }
    },
    // 医生端路由
    {
      path: '/doctor/login',
      name: 'DoctorLogin',
      component: () => import('@/views/doctor/Login.vue'),
      meta: { title: '医生登录' }
    },
    {
      path: '/doctor/register',
      name: 'DoctorRegister',
      component: () => import('@/views/doctor/Register.vue'),
      meta: { title: '医生注册' }
    },
    {
      path: '/doctor',
      name: 'DoctorHome',
      component: () => import('@/views/doctor/Home.vue'),
      meta: { title: '医生首页' }
    },
    {
      path: '/doctor/profile',
      name: 'DoctorProfile',
      component: () => import('@/views/doctor/Profile.vue'),
      meta: { title: '个人信息' }
    },
    {
      path: '/doctor/password',
      name: 'DoctorPassword',
      component: () => import('@/views/doctor/Password.vue'),
      meta: { title: '修改密码' }
    },
    {
      path: '/doctor/schedules',
      name: 'DoctorSchedules',
      component: () => import('@/views/doctor/ScheduleList.vue'),
      meta: { title: '排班管理' }
    },
    {
      path: '/doctor/schedules/create',
      name: 'DoctorScheduleCreate',
      component: () => import('@/views/doctor/ScheduleCreate.vue'),
      meta: { title: '新增排班' }
    }
  ]
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  // 医生端路由守卫
  if (to.path.startsWith('/doctor')) {
    const isAuthPage = ['/doctor/login', '/doctor/register'].includes(to.path)
    if (!userStore.token && !isAuthPage) return '/doctor/login'
    if (userStore.token && isAuthPage) return '/doctor'
    return true
  }

  // 患者端路由守卫
  const isPatientRoute = to.path.startsWith('/patient')
  if (!isPatientRoute) return true

  const isAuthPage = ['/patient/login', '/patient/register'].includes(to.path)

  if (!userStore.token && !isAuthPage) return '/patient/login'
  if (userStore.token && isAuthPage) return '/patient'
  return true
})

export default router
