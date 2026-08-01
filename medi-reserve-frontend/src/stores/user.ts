import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: sessionStorage.getItem('token') || '',
    userId: Number(sessionStorage.getItem('userId')) || null as number | null,
    username: '',
    name: '',
    phone: '',
    idCard: '',
    gender: 0,
    role: sessionStorage.getItem('userRole') || ''
  }),
  actions: {
    setToken(token: string) {
      this.token = token
      sessionStorage.setItem('token', token)
    },
    clearToken() {
      this.token = ''
      this.userId = null
      this.role = ''
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userId')
      sessionStorage.removeItem('userRole')
    },
    setUserInfo(userId: number, username: string, role: string, name = '', phone = '', idCard = '', gender = 0) {
      this.userId = userId
      sessionStorage.setItem('userId', String(userId))
      this.username = username
      this.name = name
      this.phone = phone
      this.idCard = idCard
      this.gender = gender
      this.role = role
      sessionStorage.setItem('userRole', role)
    },
    updateProfile(name: string, phone: string, idCard: string, gender: number) {
      this.name = name
      this.phone = phone
      this.idCard = idCard
      this.gender = gender
    }
  }
})