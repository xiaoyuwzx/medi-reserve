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
    }
  ]
})

router.beforeEach((to) => {
  const isPatientRoute = to.path.startsWith('/patient')
  if (!isPatientRoute) return true

  const userStore = useUserStore()
  const isAuthPage = ['/patient/login', '/patient/register'].includes(to.path)

  if (!userStore.token && !isAuthPage) return '/patient/login'
  if (userStore.token && isAuthPage) return '/patient'
  return true
})

export default router
