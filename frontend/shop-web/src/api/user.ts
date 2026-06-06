import request from './request'

// ═══ 类型 ═══
export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userId: number
  nickname: string
}

export interface SendCodeParams {
  email: string
  purpose: 'REGISTER' | 'RESET'
}

// ═══ API ═══
export function login(data: LoginParams) {
  return request.post<{ success: boolean; data: LoginResult }>('/auth/login', data)
}

export function sendCode(data: SendCodeParams) {
  return request.post('/auth/code', data)
}

export function registerByEmail(data: {
  username: string; password: string; nickname: string
  email: string; phone: string; code: string
}) {
  return request.post('/auth/register/email', data)
}

export function resetPassword(data: { email: string; code: string; password: string }) {
  return request.post('/auth/password/reset', data)
}

export function getProfile() {
  return request.get('/auth/profile')
}

export function updateProfile(data: { nickname?: string; avatarUrl?: string; email?: string; phone?: string }) {
  return request.put('/auth/profile', data)
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/auth/password', data)
}

export function logout(token: string) {
  return request.post('/auth/logout', { token })
}
