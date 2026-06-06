import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia } from 'pinia'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createMemoryHistory, createRouter } from 'vue-router'
import UserLogin from '../views/UserLogin.vue'
import RegisterView from '../views/RegisterView.vue'
import ForgotPassword from '../views/ForgotPassword.vue'

vi.mock('../api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

function installMemoryStorage() {
  const values = new Map()
  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    value: {
      clear: () => values.clear(),
      getItem: (key) => values.get(key) ?? null,
      removeItem: (key) => values.delete(key),
      setItem: (key, value) => values.set(key, value)
    }
  })
}

function plugins() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', name: 'home', component: { template: '<div />' } },
      { path: '/login', name: 'login', component: UserLogin },
      { path: '/register', name: 'register', component: RegisterView },
      { path: '/forgot-password', name: 'forgot-password', component: ForgotPassword },
      { path: '/products', name: 'products', component: { template: '<div />' } },
      { path: '/cart', name: 'cart', component: { template: '<div />' } },
      { path: '/admin/login', name: 'admin-login', component: { template: '<div />' } }
    ]
  })
  return [createPinia(), router, ElementPlus]
}

describe('store auth pages', () => {
  beforeEach(() => { installMemoryStorage() })

  it('renders login page with username/password inputs', () => {
    const wrapper = mount(UserLogin, { global: { plugins: plugins() } })
    expect(wrapper.text()).toContain('用户登录')
    expect(wrapper.find('input[placeholder="请输入用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="请输入密码"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('记住账号')
  })

  it('renders register page with email and verification code fields', () => {
    const wrapper = mount(RegisterView, { global: { plugins: plugins() } })
    expect(wrapper.text()).toContain('用户注册')
    expect(wrapper.find('input[placeholder="邮箱"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="邮箱验证码"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('发送验证码')
    expect(wrapper.find('input[placeholder="用户名"]').exists()).toBe(true)
  })

  it('renders forgot password page with email field', () => {
    const wrapper = mount(ForgotPassword, { global: { plugins: plugins() } })
    expect(wrapper.text()).toContain('找回密码')
  })
})
