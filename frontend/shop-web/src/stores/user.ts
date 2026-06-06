import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginResult, UserInfo } from '../types/index.js'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const userId = ref<number | null>(null)
  const nickname = ref<string>('')
  const email = ref<string>('')
  const avatar = ref<string>('')
  const adminId = ref<number | null>(null)
  const role = ref<string>('')

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data: LoginResult) {
    token.value = data.token
    userId.value = data.userId
    email.value = data.email
    nickname.value = data.nickname || data.email.split('@')[0]
    avatar.value = data.avatar || ''
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', String(data.userId))
    localStorage.setItem('nickname', data.nickname)
  }

  function setAdmin(data: { adminId: number; role: string; token: string }) {
    adminId.value = data.adminId
    role.value = data.role
    localStorage.setItem('adminId', String(data.adminId))
    localStorage.setItem('role', data.role)
    localStorage.setItem('token', data.token)
  }

  function setProfile(data: Partial<UserInfo>) {
    if (data.nickname) nickname.value = data.nickname
    if (data.avatar !== undefined && data.avatar !== null) avatar.value = data.avatar
    if (data.email) email.value = data.email
  }

  function logout() {
    token.value = ''
    userId.value = null
    nickname.value = ''
    email.value = ''
    avatar.value = ''
    adminId.value = null
    role.value = ''
    localStorage.clear()
  }

  // 启动时从 localStorage 恢复
  function restore() {
    const saved = localStorage.getItem('token')
    if (saved) {
      token.value = saved
      userId.value = Number(localStorage.getItem('userId')) || null
      nickname.value = localStorage.getItem('nickname') || ''
      adminId.value = Number(localStorage.getItem('adminId')) || null
      role.value = localStorage.getItem('role') || ''
    }
  }

  restore()

  return { token, userId, nickname, email, avatar, adminId, role, isLoggedIn, setAuth, setAdmin, setProfile, logout }
})
