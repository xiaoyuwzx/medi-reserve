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
    // 在线问诊
    {
      path: '/consultation/:appointmentId',
      name: 'ConsultationRoom',
      component: () => import('@/views/consultation/Room.vue'),
      meta: { title: '在线问诊' }
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
    },
    {
      path: '/doctor/patients',
      name: 'DoctorPatients',
      component: () => import('@/views/doctor/PatientList.vue'),
      meta: { title: '问诊患者' }
    },
    {
      path: '/doctor/statistics',
      name: 'DoctorStatistics',
      component: () => import('@/views/doctor/Statistics.vue'),
      meta: { title: '数据统计' }
    },
    {
      path: '/doctor/evaluations',
      name: 'DoctorEvaluations',
      component: () => import('@/views/doctor/Evaluations.vue'),
      meta: { title: '患者评价' }
    },
    // 管理端路由
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/Login.vue'),
      meta: { title: '管理员登录' }
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      children: [
        {
          path: '',
          name: 'AdminHome',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'audit/doctors',
          name: 'AdminDoctorAudit',
          component: () => import('@/views/admin/DoctorAuditList.vue'),
          meta: { title: '医生审核' }
        },
        {
          path: 'audit/doctors/:doctorId',
          name: 'AdminDoctorAuditDetail',
          component: () => import('@/views/admin/DoctorAuditDetail.vue'),
          meta: { title: '审核详情' }
        },
        {
          path: 'audit/cert',
          name: 'AdminCertAudit',
          component: () => import('@/views/admin/CertAuditList.vue'),
          meta: { title: '证件审核' }
        },
        {
          path: 'audit/cert/:doctorId',
          name: 'AdminCertAuditDetail',
          component: () => import('@/views/admin/CertAuditDetail.vue'),
          meta: { title: '证件审核详情' }
        },
        {
          path: 'admins',
          name: 'AdminList',
          component: () => import('@/views/admin/AdminList.vue'),
          meta: { title: '管理员管理' }
        },
        {
          path: 'password',
          name: 'AdminPassword',
          component: () => import('@/views/admin/Password.vue'),
          meta: { title: '修改密码' }
        },
        {
          path: 'doctors',
          name: 'AdminDoctorManage',
          component: () => import('@/views/admin/DoctorManageList.vue'),
          meta: { title: '医生管理' }
        },
        {
          path: 'patients',
          name: 'AdminPatientManage',
          component: () => import('@/views/admin/PatientManageList.vue'),
          meta: { title: '患者管理' }
        },
        {
          path: 'logs',
          name: 'AdminLogList',
          component: () => import('@/views/admin/LogList.vue'),
          meta: { title: '操作日志' }
        },
        {
          path: 'logs/:id',
          name: 'AdminLogDetail',
          component: () => import('@/views/admin/LogDetail.vue'),
          meta: { title: '日志详情' }
        },
        {
          path: 'permissions/tree',
          name: 'AdminPermissionTree',
          component: () => import('@/views/admin/PermissionTree.vue'),
          meta: { title: '权限树' }
        },
        {
          path: 'permissions/roles',
          name: 'AdminRolePermission',
          component: () => import('@/views/admin/RolePermission.vue'),
          meta: { title: '角色权限' }
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  // 管理端路由守卫
  if (to.path.startsWith('/admin')) {
    const isAuthPage = to.path === '/admin/login'
    if (!userStore.token && !isAuthPage) return '/admin/login'
    if (userStore.token && isAuthPage) return '/admin'
    return true
  }

  // 问诊室路由守卫（需登录）
  if (to.path.startsWith('/consultation')) {
    if (!userStore.token) return '/patient/login'
    return true
  }

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