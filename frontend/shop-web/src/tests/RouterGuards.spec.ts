/**
 * 路由守卫测试 —— 覆盖认证拦截、guest 页面跳转、权限校验
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user'

// Mock localStorage
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
      { path: '/', name: 'home', component: { template: '<div>Home</div>' }, meta: { title: '首页' } },
      { path: '/login', name: 'login', component: { template: '<div>Login</div>' }, meta: { title: '登录', guest: true } },
      { path: '/cart', name: 'cart', component: { template: '<div>Cart</div>' }, meta: { title: '购物车', auth: true } },
      { path: '/user/profile', name: 'profile', component: { template: '<div>Profile</div>' }, meta: { title: '个人信息', auth: true } },
    ],
  })
}

describe('路由守卫', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    Object.keys(store).forEach((k) => delete store[k])
  })

  it('未登录时访问需要认证的页面 → 重定向到 /login', async () => {
    const router = makeRouter()
    const userStore = useUserStore()

    router.beforeEach((to) => {
      if (to.meta.auth && !userStore.isLoggedIn) return { path: '/login', query: { redirect: to.fullPath } }
      if (to.meta.guest && userStore.isLoggedIn) return '/'
      return true
    })

    await router.push('/cart')
    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/cart')
  })

  it('已登录访问登录页 → 重定向到首页', async () => {
    const router = makeRouter()
    const userStore = useUserStore()
    store['token'] = 'test-token'
    userStore.setAuth({ token: 'test-token', userId: 1, email: 'test@test.com', nickname: 'Test' })

    router.beforeEach((to) => {
      if (to.meta.auth && !userStore.isLoggedIn) return { path: '/login', query: { redirect: to.fullPath } }
      if (to.meta.guest && userStore.isLoggedIn) return '/'
      return true
    })

    await router.push('/login')
    expect(router.currentRoute.value.path).toBe('/')
  })

  it('已登录访问需要认证的页面 → 放行', async () => {
    const router = makeRouter()
    const userStore = useUserStore()
    store['token'] = 'test-token'
    userStore.setAuth({ token: 'test-token', userId: 1, email: 'test@test.com', nickname: 'Test' })

    router.beforeEach((to) => {
      if (to.meta.auth && !userStore.isLoggedIn) return { path: '/login', query: { redirect: to.fullPath } }
      if (to.meta.guest && userStore.isLoggedIn) return '/'
      return true
    })

    await router.push('/cart')
    expect(router.currentRoute.value.path).toBe('/cart')
  })

  it('未登录访问公开页面 → 放行', async () => {
    const router = makeRouter()

    router.beforeEach((to) => {
      if (to.meta.auth && !useUserStore().isLoggedIn) return { path: '/login', query: { redirect: to.fullPath } }
      return true
    })

    await router.push('/')
    expect(router.currentRoute.value.path).toBe('/')
  })
})

describe('UserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    Object.keys(store).forEach((k) => delete store[k])
  })

  it('setAuth 后 isLoggedIn 为 true', () => {
    const userStore = useUserStore()
    userStore.setAuth({ token: 'abc', userId: 1, email: 'a@b.com', nickname: 'Alice' })
    expect(userStore.isLoggedIn).toBe(true)
    expect(userStore.userId).toBe(1)
    expect(userStore.nickname).toBe('Alice')
  })

  it('logout 后 isLoggedIn 为 false', () => {
    const userStore = useUserStore()
    userStore.setAuth({ token: 'abc', userId: 1, email: 'a@b.com', nickname: 'Alice' })
    userStore.logout()
    expect(userStore.isLoggedIn).toBe(false)
    expect(userStore.token).toBe('')
  })

  it('从 localStorage 恢复登录态', () => {
    store['token'] = 'saved-token'
    store['userId'] = '2'
    store['nickname'] = 'Bob'

    setActivePinia(createPinia())
    const userStore = useUserStore()
    // restore() 在构造时自动调用
    expect(userStore.isLoggedIn).toBe(true)
    expect(userStore.userId).toBe(2)
  })
})
