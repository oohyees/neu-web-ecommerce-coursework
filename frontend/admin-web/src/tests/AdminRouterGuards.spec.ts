/**
 * 管理后台路由守卫测试
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { useAdminStore } from '@/stores/admin'

const store: Record<string, any> = {}
vi.stubGlobal('localStorage', {
  getItem: (key: any) => store[key] ?? null,
  setItem: (key: string, value: string) => { store[key] = value },
  removeItem: (key: any) => { delete store[key] },
  clear: () => { Object.keys(store).forEach((k) => delete store[k]) },
})

function makeRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/admin/login', name: 'login', component: { template: '<div/>' }, meta: { title: '登录', guest: true } },
      { path: '/admin/dashboard', name: 'dashboard', component: { template: '<div/>' }, meta: { title: '看板', auth: true } },
      { path: '/admin/users', name: 'users', component: { template: '<div/>' }, meta: { title: '用户', auth: true, superAdmin: true } },
      { path: '/admin/products', name: 'products', component: { template: '<div/>' }, meta: { title: '商品', auth: true } },
    ],
  })
}

describe('Admin 路由守卫', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    Object.keys(store).forEach((k) => delete store[k])
  })

  it('未登录访问 dashboard → 重定向到 /admin/login', async () => {
    const router = makeRouter()
    const adminStore = useAdminStore()

    router.beforeEach((to) => {
      if (to.meta.auth && !adminStore.isLoggedIn) return { path: '/admin/login' }
      if (to.meta.guest && adminStore.isLoggedIn) return '/admin/dashboard'
      return true
    })

    await router.push('/admin/dashboard')
    expect(router.currentRoute.value.path).toBe('/admin/login')
  })

  it('已登录访问 login → 重定向到 dashboard', async () => {
    const router = makeRouter()
    const adminStore = useAdminStore()
    store['token'] = 'admin-token'
    store['adminId'] = '1'
    store['role'] = 'ADMIN'
    adminStore.setAuth({ token: 'admin-token', adminId: 1, username: 'admin', name: 'Admin', role: 'ADMIN', permissions: ['product:manage'] })

    router.beforeEach((to) => {
      if (to.meta.auth && !adminStore.isLoggedIn) return { path: '/admin/login' }
      if (to.meta.guest && adminStore.isLoggedIn) return '/admin/dashboard'
      return true
    })

    await router.push('/admin/login')
    expect(router.currentRoute.value.path).toBe('/admin/dashboard')
  })

  it('非超级管理员访问 superAdmin 页面 → 重定向', async () => {
    const router = makeRouter()
    const adminStore = useAdminStore()
    store['token'] = 'admin-token'
    store['adminId'] = '1'
    store['role'] = 'ADMIN'
    adminStore.setAuth({ token: 'admin-token', adminId: 1, username: 'admin', name: 'Admin', role: 'ADMIN', permissions: [] })

    router.beforeEach((to) => {
      if (to.meta.auth && !adminStore.isLoggedIn) return { path: '/admin/login' }
      if (to.meta.superAdmin && adminStore.role !== 'SUPER_ADMIN') return '/admin/dashboard'
      return true
    })

    await router.push('/admin/users')
    expect(router.currentRoute.value.path).toBe('/admin/dashboard')
  })

  it('超级管理员访问 superAdmin 页面 → 放行', async () => {
    const router = makeRouter()
    const adminStore = useAdminStore()
    store['token'] = 'admin-token'
    store['adminId'] = '1'
    store['role'] = 'SUPER_ADMIN'
    adminStore.setAuth({ token: 'admin-token', adminId: 1, username: 'super', name: 'Super', role: 'SUPER_ADMIN', permissions: [] })

    router.beforeEach((to) => {
      if (to.meta.auth && !adminStore.isLoggedIn) return { path: '/admin/login' }
      if (to.meta.superAdmin && adminStore.role !== 'SUPER_ADMIN') return '/admin/dashboard'
      return true
    })

    await router.push('/admin/users')
    expect(router.currentRoute.value.path).toBe('/admin/users')
  })

  it('hasPermission 对普通管理员做精确校验', () => {
    const adminStore = useAdminStore()
    adminStore.setAuth({ token: 't', adminId: 1, username: 'a', name: 'A', role: 'ADMIN', permissions: ['product:manage'] })
    expect(adminStore.hasPermission('product:manage')).toBe(true)
    expect(adminStore.hasPermission('user:manage')).toBe(false)
  })

  it('SUPER_ADMIN 拥有所有权限', () => {
    const adminStore = useAdminStore()
    adminStore.setAuth({ token: 't', adminId: 1, username: 's', name: 'S', role: 'SUPER_ADMIN', permissions: [] })
    expect(adminStore.hasPermission('anything')).toBe(true)
  })
})
