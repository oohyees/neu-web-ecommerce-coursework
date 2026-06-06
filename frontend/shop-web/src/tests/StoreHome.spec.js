import { describe, expect, it, vi } from 'vitest'
import { createPinia } from 'pinia'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createMemoryHistory, createRouter } from 'vue-router'
import HomeView from '../views/HomeView.vue'

vi.mock('../api', () => ({
  api: {
    get: vi.fn((url) => {
      if (url === '/home') return Promise.resolve({ data: { data: { banners: [], hotProducts: [], newProducts: [], hotSearches: [] } } })
      if (url === '/categories') return Promise.resolve({ data: { data: [] } })
      if (url === '/marketing/coupons') return Promise.resolve({ data: { data: [] } })
      return Promise.resolve({ data: { data: [] } })
    }),
    post: vi.fn().mockResolvedValue({ data: { success: true } }),
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
      getItem: () => null,
      removeItem: () => {},
      setItem: () => {}
    }
  })
}

function plugins() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', name: 'home', component: HomeView },
      { path: '/products', name: 'products', component: { template: '<div />' } },
      { path: '/login', name: 'login', component: { template: '<div />' } }
    ]
  })
  return [createPinia(), router, ElementPlus]
}

describe('store home page', () => {
  it('renders home page with search box and hot search section', async () => {
    installMemoryStorage()
    const wrapper = mount(HomeView, { global: { plugins: plugins() } })
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('搜索')
    expect(wrapper.text()).toContain('热门搜索')
  })

  it('renders category panel section', async () => {
    installMemoryStorage()
    const wrapper = mount(HomeView, { global: { plugins: plugins() } })
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('全部分类')
  })
})
