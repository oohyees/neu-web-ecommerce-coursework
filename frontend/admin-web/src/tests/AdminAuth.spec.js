import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia } from 'pinia'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createMemoryHistory, createRouter } from 'vue-router'
import AdminLogin from '../views/AdminLogin.vue'

vi.mock('../api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn().mockResolvedValue({ data: { success: true, data: { token: 'tk', adminId: 1, role: 'SUPER_ADMIN' } } }),
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
      { path: '/admin/login', name: 'admin-login', component: AdminLogin },
      { path: '/admin/dashboard', component: { template: '<div />' } }
    ]
  })
  return [createPinia(), router, ElementPlus]
}

describe('admin auth pages', () => {
  beforeEach(() => { installMemoryStorage() })

  it('renders admin login page with username/password', () => {
    const wrapper = mount(AdminLogin, { global: { plugins: plugins() } })
    expect(wrapper.text()).toContain('管理员登录')
    expect(wrapper.find('input[placeholder="用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="密码"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('登录后台')
  })
})
