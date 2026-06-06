import { describe, expect, it, vi } from 'vitest'
import { createPinia } from 'pinia'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createMemoryHistory, createRouter } from 'vue-router'
import AdminDashboard from '../views/AdminDashboard.vue'

vi.mock('../api', () => ({
  api: {
    get: vi.fn((url) => {
      if (url === '/admin/dashboard') return Promise.resolve({
        data: { success: true, data: { totalUsers: 100, totalOrders: 50, totalSales: 9999, todayOrders: 3, todaySales: 299, trend: [], topProducts: [], pendingFeedback: 0, pendingShipment: 0 } }
      })
      return Promise.resolve({ data: { success: true, data: [] } })
    }),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

function installMemoryStorage() {
  const values = new Map([['token', 'tk'], ['adminId', '1'], ['role', 'SUPER_ADMIN']])
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
      { path: '/admin/dashboard', name: 'dashboard', component: AdminDashboard },
      { path: '/admin/orders', component: { template: '<div />' } }
    ]
  })
  return [createPinia(), router, ElementPlus]
}

describe('admin dashboard', () => {
  it('renders dashboard with stats cards', async () => {
    installMemoryStorage()
    const wrapper = mount(AdminDashboard, { global: { plugins: plugins() } })
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('数据看板')
    expect(wrapper.text()).toContain('销售额')
    expect(wrapper.text()).toContain('今日销售额')
  })
})
