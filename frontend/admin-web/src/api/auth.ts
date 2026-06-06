import request from './request'

export interface AdminLoginParams {
  username: string
  password: string
}

export interface AdminLoginResult {
  token: string
  adminId: number
  role: string
}

export function adminLogin(data: AdminLoginParams) {
  return request.post<{ success: boolean; data: AdminLoginResult }>('/auth/admin/login', data)
}

export function adminLogout(token: string) {
  return request.post('/auth/logout', { token })
}

export function fetchAdminProfile() {
  return request.get('/auth/admin/profile')
}

export function updateAdminProfile(data: { nickname?: string; email?: string; phone?: string }) {
  return request.put('/auth/admin/profile', data)
}

export function changeAdminPassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/auth/admin/password', data)
}
