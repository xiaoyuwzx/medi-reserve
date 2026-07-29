import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: null as number | null,
    username: '',
    name: '',
    phone: '',
    idCard: '',
    gender: 0,
    role: ''
  }),
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },
    clearToken() {
      this.token = ''
      localStorage.removeItem('token')
    },
    setUserInfo(userId: number, username: string, role: string, name = '', phone = '', idCard = '', gender = 0) {
      this.userId = userId
      this.username = username
      this.name = name
      this.phone = phone
      this.idCard = idCard
      this.gender = gender
      this.role = role
    },
    updateProfile(name: string, phone: string, idCard: string, gender: number) {
      this.name = name
      this.phone = phone
      this.idCard = idCard
      this.gender = gender
    }
  }
})