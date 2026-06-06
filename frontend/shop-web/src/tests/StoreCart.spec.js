import { describe, expect, it, vi } from 'vitest'
import { createPinia } from 'pinia'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createMemoryHistory, createRouter } from 'vue-router'
import CartView from '../views/CartView.vue'

vi.mock('../api', () => ({
  api: {
    get: vi.fn().mockResolvedValue({ data: { success: true, data: [] } }),
    post: vi.fn().mockResolvedValue({ data: { success: true } }),
    put: vi.fn().mockResolvedValue({ data: { success: true } }),
    delete: vi.fn().mockResolvedValue({ data: { success: true } })
  }
}))

function installMemoryStorage() {
  const values = new Map([['token', 'test-token'], ['userId', '1'], ['nickname', 'Alice']])
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
      { path: '/cart', name: 'cart', component: CartView },
      { path: '/checkout', name: 'checkout', component: { template: '<div />' } },
      { path: '/products/:id', name: 'product-detail', component: { template: '<div />' } },
      { path: '/login', name: 'login', component: { template: '<div />' } }
    ]
  })
  return [createPinia(), router, ElementPlus]
}

describe('cart view', () => {
  it('renders cart page with header and empty state', async () => {
    installMemoryStorage()
    const wrapper = mount(CartView, { global: { plugins: plugins() } })
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('购物车')
    expect(wrapper.text()).toContain('继续购物')
  })

  it('shows select all toggle', async () => {
    installMemoryStorage()
    const wrapper = mount(CartView, { global: { plugins: plugins() } })
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('全选')
    expect(wrapper.text()).toContain('反选')
  })
})
