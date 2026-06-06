import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface AdminLoginResult {
  token: string
  adminId: number
  role: string
}

export const useAdminStore = defineStore('admin', () => {
  const token = ref('')
  const adminId = ref<number | null>(null)
  const username = ref('')
  const role = ref('')
  const permissions = ref<string[]>([])

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data: AdminLoginResult) {
    token.value = data.token
    adminId.value = data.adminId
    role.value = data.role
    if (data.role === 'SUPER_ADMIN') {
      permissions.value = ['*']
    }
  }

  function updateSession(data: { adminId: number; role: string }) {
    adminId.value = data.adminId
    role.value = data.role
    if (data.role === 'SUPER_ADMIN') {
      permissions.value = ['*']
    }
  }

  function hasPermission(code: string): boolean {
    return role.value === 'SUPER_ADMIN' || permissions.value.includes(code)
  }

  function logout() {
    token.value = ''
    adminId.value = null
    username.value = ''
    role.value = ''
    permissions.value = []
  }

  return { token, adminId, username, role, permissions, isLoggedIn, setAuth, updateSession, hasPermission, logout }
}, { persist: true })
