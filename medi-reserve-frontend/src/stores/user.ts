import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: sessionStorage.getItem('token') || '',
    userId: Number(sessionStorage.getItem('userId')) || null as number | null,
    username: sessionStorage.getItem('username') || '',
    name: sessionStorage.getItem('userName') || '',
    phone: sessionStorage.getItem('userPhone') || '',
    idCard: sessionStorage.getItem('userIdCard') || '',
    gender: Number(sessionStorage.getItem('userGender')) || 0,
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
      this.username = ''
      this.name = ''
      this.phone = ''
      this.idCard = ''
      this.gender = 0
      this.role = ''
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userId')
      sessionStorage.removeItem('username')
      sessionStorage.removeItem('userName')
      sessionStorage.removeItem('userPhone')
      sessionStorage.removeItem('userIdCard')
      sessionStorage.removeItem('userGender')
      sessionStorage.removeItem('userRole')
    },
    setUserInfo(userId: number, username: string, role: string, name = '', phone = '', idCard = '', gender = 0) {
      this.userId = userId
      this.username = username
      this.name = name
      this.phone = phone
      this.idCard = idCard
      this.gender = gender
      this.role = role
      sessionStorage.setItem('userId', String(userId))
      sessionStorage.setItem('username', username)
      sessionStorage.setItem('userName', name)
      sessionStorage.setItem('userPhone', phone)
      sessionStorage.setItem('userIdCard', idCard)
      sessionStorage.setItem('userGender', String(gender))
      sessionStorage.setItem('userRole', role)
    },
    updateProfile(name: string, phone: string, idCard: string, gender: number) {
      this.name = name
      this.phone = phone
      this.idCard = idCard
      this.gender = gender
      sessionStorage.setItem('userName', name)
      sessionStorage.setItem('userPhone', phone)
      sessionStorage.setItem('userIdCard', idCard)
      sessionStorage.setItem('userGender', String(gender))
    }
  }
})