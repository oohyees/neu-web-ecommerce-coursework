import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginResult } from '@/api/user'
import { markFreshLogin, consumeFreshLogin } from '@/utils/freshLogin'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userId = ref<number | null>(null)
  const nickname = ref('')
  const avatarUrl = ref('')
  const email = ref('')
  const phone = ref('')

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data: LoginResult) {
    token.value = data.token
    userId.value = data.userId
    nickname.value = data.nickname || ''
    markFreshLogin()
  }

  function setProfile(data: { nickname?: string; avatarUrl?: string; email?: string; phone?: string }) {
    if (data.nickname !== undefined) nickname.value = data.nickname
    if (data.avatarUrl !== undefined) avatarUrl.value = data.avatarUrl
    if (data.email !== undefined) email.value = data.email
    if (data.phone !== undefined) phone.value = data.phone
  }

  function logout() {
    token.value = ''
    userId.value = null
    nickname.value = ''
    avatarUrl.value = ''
    email.value = ''
    phone.value = ''
    consumeFreshLogin()
  }

  return { token, userId, nickname, avatarUrl, email, phone, isLoggedIn, setAuth, setProfile, logout }
}, { persist: true })
