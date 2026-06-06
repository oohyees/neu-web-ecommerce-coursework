import axios from 'axios'
import type { ApiResponse } from '../types/index'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

// 请求拦截器：自动附加 token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('nickname')
      localStorage.removeItem('adminId')
      localStorage.removeItem('role')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

// 类型安全的请求辅助函数
export async function get<T>(url: string, params?: Record<string, any>): Promise<ApiResponse<T>> {
  const res = await http.get<ApiResponse<T>>(url, { params })
  return res.data
}

export async function post<T>(url: string, data?: any, params?: Record<string, any>): Promise<ApiResponse<T>> {
  const res = await http.post<ApiResponse<T>>(url, data, { params })
  return res.data
}

export async function put<T>(url: string, data?: any): Promise<ApiResponse<T>> {
  const res = await http.put<ApiResponse<T>>(url, data)
  return res.data
}

export async function del<T>(url: string, config?: Record<string, any>): Promise<ApiResponse<T>> {
  const res = await http.delete<ApiResponse<T>>(url, { params: config?.params, ...config })
  return res.data
}

// 向后兼容：保留 api 对象
export const api = http
