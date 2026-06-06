import { post, get } from './request'
import type { LoginParams, RegisterParams, LoginResult, UserInfo } from '../types/index'

export function login(params: LoginParams) {
  return post<LoginResult>('/auth/login', params)
}

export function register(params: RegisterParams) {
  return post<void>('/auth/register', params)
}

export function logout() {
  return post<void>('/auth/logout', { token: localStorage.getItem('token') || '' })
}

export function forgotPassword(email: string) {
  return post<void>('/auth/forgot-password', { email })
}

export function sendCaptcha(email: string) {
  return post<void>('/auth/captcha', { email })
}

export function getCurrentUser() {
  return get<UserInfo>('/auth/me')
}

export function updateProfile(data: Partial<UserInfo>) {
  return post<UserInfo>('/auth/profile', data)
}

export function changePassword(oldPassword: string, newPassword: string) {
  return post<void>('/auth/change-password', { oldPassword, newPassword })
}
