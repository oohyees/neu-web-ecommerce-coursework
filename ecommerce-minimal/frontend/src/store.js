import { defineStore } from 'pinia'
export const useSessionStore = defineStore('session', {
  state: () => ({
    userId: Number(localStorage.getItem('userId')) || null,
    nickname: localStorage.getItem('nickname') || '',
    adminId: Number(localStorage.getItem('adminId')) || null,
    role: localStorage.getItem('role') || ''
  }),
  actions: {
    setUser(data) {
      this.userId = data.userId; this.nickname = data.nickname
      localStorage.setItem('userId', data.userId); localStorage.setItem('nickname', data.nickname); localStorage.setItem('token', data.token)
    },
    setAdmin(data) {
      this.adminId = data.adminId; this.role = data.role
      localStorage.setItem('adminId', data.adminId); localStorage.setItem('role', data.role); localStorage.setItem('token', data.token)
    },
    logout() {
      this.userId = null; this.nickname = ''; this.adminId = null; this.role = ''
      localStorage.clear()
    }
  }
})
