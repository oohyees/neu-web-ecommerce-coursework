import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface AdminUser {
  adminId: number
  username: string
  name: string
  role: 'ADMIN' | 'SUPER_ADMIN'
  permissions: string[]
}

export interface AdminLoginResult {
  token: string
  adminId: number
  username: string
  name: string
  role: 'ADMIN' | 'SUPER_ADMIN'
  permissions: string[]
}

export const useAdminStore = defineStore('admin', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const adminId = ref<number | null>(Number(localStorage.getItem('adminId')) || null)
  const username = ref<string>(localStorage.getItem('adminUsername') || '')
  const name = ref<string>(localStorage.getItem('adminName') || '')
  const role = ref<string>(localStorage.getItem('role') || '')
  const permissions = ref<string[]>(JSON.parse(localStorage.getItem('adminPermissions') || '[]'))

  const isLoggedIn = computed(() => !!token.value && !!adminId.value)

  function setAuth(data: AdminLoginResult) {
    token.value = data.token
    adminId.value = data.adminId
    username.value = data.username
    name.value = data.name
    role.value = data.role
    permissions.value = data.permissions || []
    localStorage.setItem('token', data.token)
    localStorage.setItem('adminId', String(data.adminId))
    localStorage.setItem('adminUsername', data.username)
    localStorage.setItem('adminName', data.name)
    localStorage.setItem('role', data.role)
    localStorage.setItem('adminPermissions', JSON.stringify(data.permissions || []))
  }

  function hasPermission(code: string): boolean {
    return role.value === 'SUPER_ADMIN' || permissions.value.includes(code)
  }

  function logout() {
    token.value = ''
    adminId.value = null
    username.value = ''
    name.value = ''
    role.value = ''
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('adminId')
    localStorage.removeItem('adminUsername')
    localStorage.removeItem('adminName')
    localStorage.removeItem('role')
    localStorage.removeItem('adminPermissions')
  }

  return { token, adminId, username, name, role, permissions, isLoggedIn, setAuth, hasPermission, logout }
})
