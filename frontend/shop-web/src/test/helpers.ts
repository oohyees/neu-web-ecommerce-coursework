import { vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'

/**
 * Create a fresh Pinia instance for each test
 */
export function createTestPinia() {
  const pinia = createPinia()
  setActivePinia(pinia)
  return pinia
}

/**
 * Helper to create a router mock with custom params/query
 */
export function createRouterMock(params: Record<string, any> = {}, query: Record<string, any> = {}) {
  const push = vi.fn()
  const replace = vi.fn()
  return {
    push,
    replace,
    go: vi.fn(),
    back: vi.fn(),
    currentRoute: {
      value: { params, query, path: '/', fullPath: '/', meta: {} },
    },
  }
}

/**
 * Helper to flush all pending promises
 */
export async function flushPromises() {
  await new Promise(resolve => setTimeout(resolve, 0))
}

/**
 * Mount a component with common plugins
 */
export function mountWithPlugins(
  component: any,
  options: Record<string, any> = {},
) {
  const pinia = createPinia()
  setActivePinia(pinia)

  return mount(component, {
    global: {
      plugins: [pinia],
      stubs: {
        ElButton: true,
        ElInput: true,
        ElForm: true,
        ElFormItem: true,
        ElSkeleton: true,
        ElEmpty: true,
        ElCheckbox: true,
        ElSwitch: true,
        ElSelect: true,
        ElOption: true,
        ElTable: true,
        ElTableColumn: true,
        ElPagination: true,
        ElDialog: true,
        ElInputNumber: true,
        ElRate: true,
        ElCarousel: true,
        ElCarouselItem: true,
        ElTabs: true,
        ElTabPane: true,
        ElRadioButton: true,
        ElRadioGroup: true,
        RouterLink: true,
        RouterView: true,
        ...options.stubs,
      },
      mocks: {
        $router: createRouterMock(),
        $route: { params: {}, query: {}, path: '/', meta: {} },
        ...options.mocks,
      },
    },
    ...options,
  })
}
