import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import router from '@/router'
import { resolveMock } from './mock'

const staticPreview = import.meta.env.VITE_STATIC_PREVIEW === 'true'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const adminStore = useAdminStore()
  if (adminStore.token) {
    config.headers.Authorization = `Bearer ${adminStore.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body.success === false) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message))
    }
    return body as any
  },
  (error) => {
    if (error.response?.status === 401) {
      const adminStore = useAdminStore()
      adminStore.logout()
      const path = router.currentRoute.value.path
      if (path !== '/login') {
        ElMessage.warning('登录已过期，请重新登录')
        router.push({ path: '/login' })
      }
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

function withDataAlias<T>(payload: T): T {
  if (payload && typeof payload === 'object' && !Object.prototype.hasOwnProperty.call(payload, 'data')) {
    Object.defineProperty(payload, 'data', {
      value: payload,
      configurable: true,
    })
  }
  return payload
}

function normalizeParams(input?: any) {
  return input && typeof input === 'object' && Object.prototype.hasOwnProperty.call(input, 'params')
    ? input.params
    : input
}

const request = {
  get<T = any>(url: string, params?: any): Promise<T> {
    if (staticPreview) return Promise.resolve(withDataAlias(resolveMock(url, 'get', normalizeParams(params)) as T))
    return http.get(url, { params: normalizeParams(params) }).then((res: any) => withDataAlias(res.data as T))
  },
  post<T = any>(url: string, data?: any, config?: any): Promise<T> {
    if (staticPreview) return Promise.resolve(withDataAlias(resolveMock(url, 'post', data) as T))
    return http.post(url, data, config).then((res: any) => withDataAlias(res.data as T))
  },
  put<T = any>(url: string, data?: any, config?: any): Promise<T> {
    if (staticPreview) return Promise.resolve(withDataAlias(resolveMock(url, 'put', data) as T))
    return http.put(url, data, config).then((res: any) => withDataAlias(res.data as T))
  },
  delete<T = any>(url: string, config?: any): Promise<T> {
    if (staticPreview) return Promise.resolve(withDataAlias(resolveMock(url, 'delete', normalizeParams(config)) as T))
    return http.delete(url, {
      ...config,
      params: normalizeParams(config),
    }).then((res: any) => withDataAlias(res.data as T))
  },
}

export default request
