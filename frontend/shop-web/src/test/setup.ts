import { vi } from 'vitest'
import { config } from '@vue/test-utils'

// Mock Element Plus components that are commonly used
config.global.stubs = {
  ElButton: true,
  ElInput: true,
  ElForm: true,
  ElFormItem: true,
  ElMessage: true,
  ElMessageBox: true,
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
}

// Mock Element Plus ElMessage
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    warning: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
  },
  ElMessageBox: {
    confirm: vi.fn(() => Promise.resolve()),
  },
}))

// Mock vue-router
const mockPush = vi.fn()
const mockReplace = vi.fn()
const mockGo = vi.fn()
const mockBack = vi.fn()

vi.mock('vue-router', () => ({
  createRouter: vi.fn(),
  createWebHistory: vi.fn(),
  useRouter: () => ({
    push: mockPush,
    replace: mockReplace,
    go: mockGo,
    back: mockBack,
  }),
  useRoute: () => ({
    params: {},
    query: {},
    path: '/',
    fullPath: '/',
    meta: {},
  }),
  RouterLink: true,
  RouterView: true,
}))

// Export mocks for use in tests
export { mockPush, mockReplace, mockGo, mockBack }
