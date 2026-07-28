import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: null as number | null,
    username: '',
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
    setUserInfo(userId: number, username: string, role: string) {
      this.userId = userId
      this.username = username
      this.role = role
    }
  }
})