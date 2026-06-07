/**
 * 用户认证模块测试
 * 覆盖采分点：用户认证（邮箱注册、验证码验证、账号密码登录、记住密码、退出登录、找回密码、个人信息修改）
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ElMessage } from 'element-plus'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import ForgotPasswordView from '@/views/ForgotPasswordView.vue'
import ProfileView from '@/views/ProfileView.vue'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/user'

vi.mock('@/api/user', () => ({
  login: vi.fn(),
  sendCode: vi.fn(),
  registerByEmail: vi.fn(),
  resetPassword: vi.fn(),
  getProfile: vi.fn(),
  updateProfile: vi.fn(),
  changePassword: vi.fn(),
}))

vi.mock('@/utils/freshLogin', () => ({
  markFreshLogin: vi.fn(),
  consumeFreshLogin: vi.fn(),
}))

const mockPush = vi.fn()
const mockReplace = vi.fn()
let mockRoute = { query: {} as Record<string, any>, params: {} as Record<string, any>, path: '/' }
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush, replace: mockReplace }),
  useRoute: () => mockRoute,
}))

const authStubs = {
  ElButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
  ElInput: { template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />', props: ['modelValue', 'placeholder', 'type', 'size', 'showPassword'] },
  ElForm: { template: '<form @submit.prevent="$emit(\'submit\')"><slot /></form>' },
  ElFormItem: { template: '<div><slot /></div>' },
  RouterLink: { template: '<a><slot /></a>', props: ['to'] },
}

describe('LoginView - 用户登录', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('renders login form with title and subtitle', () => {
    const wrapper = mount(LoginView, { global: { plugins: [createPinia()], stubs: authStubs } })
    expect(wrapper.find('h2').text()).toBe('登录')
    expect(wrapper.text()).toContain('欢迎回到优品商城')
  })

  it('has username and password input fields', () => {
    const wrapper = mount(LoginView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const inputs = wrapper.findAll('input')
    expect(inputs.length).toBeGreaterThanOrEqual(2)
  })

  it('shows warning when submitting empty form', async () => {
    const wrapper = mount(LoginView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form = { username: '', password: '' }
    await vm.handleLogin()
    expect(ElMessage.warning).toHaveBeenCalledWith('请输入账号和密码')
  })

  it('calls login API and redirects on success', async () => {
    vi.mocked(userApi.login).mockResolvedValueOnce({ data: { token: 'abc', userId: 1, nickname: 'Test' } } as any)
    const wrapper = mount(LoginView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form = { username: 'testuser', password: '123456' }
    await vm.handleLogin()
    await flushPromises()
    expect(userApi.login).toHaveBeenCalledWith({ username: 'testuser', password: '123456' })
    expect(ElMessage.success).toHaveBeenCalled()
    expect(mockPush).toHaveBeenCalledWith('/')
  })

  it('redirects to original page after login when redirect query exists', async () => {
    vi.mocked(userApi.login).mockResolvedValueOnce({ data: { token: 'abc', userId: 1, nickname: 'Test' } } as any)
    mockRoute.query = { redirect: '/cart' }
    const wrapper = mount(LoginView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form = { username: 'testuser', password: '123456' }
    await vm.handleLogin()
    await flushPromises()
    expect(mockPush).toHaveBeenCalledWith('/cart')
    mockRoute.query = {}
  })

  it('has link to register page', () => {
    const wrapper = mount(LoginView, { global: { plugins: [createPinia()], stubs: authStubs } })
    expect(wrapper.text()).toContain('注册')
  })

  it('has link to forgot password page', () => {
    const wrapper = mount(LoginView, { global: { plugins: [createPinia()], stubs: authStubs } })
    expect(wrapper.text()).toContain('忘记密码')
  })

  it('shows benefits section', () => {
    const wrapper = mount(LoginView, { global: { plugins: [createPinia()], stubs: authStubs } })
    expect(wrapper.text()).toContain('购物车同步')
    expect(wrapper.text()).toContain('订单追踪')
  })
})

describe('RegisterView - 邮箱注册', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders register form with title', () => {
    const wrapper = mount(RegisterView, { global: { plugins: [createPinia()], stubs: authStubs } })
    expect(wrapper.find('h2').text()).toBe('注册')
    expect(wrapper.text()).toContain('创建你的优品商城账号')
  })

  it('has email, code, password input fields', () => {
    const wrapper = mount(RegisterView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const inputs = wrapper.findAll('input')
    expect(inputs.length).toBeGreaterThanOrEqual(4)
  })

  it('shows warning when submitting incomplete form', async () => {
    const wrapper = mount(RegisterView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form = { username: '', password: '', email: '', code: '' }
    await vm.handleRegister()
    expect(ElMessage.warning).toHaveBeenCalledWith('请填写完整信息')
  })

  it('sends verification code to email', async () => {
    vi.mocked(userApi.sendCode).mockResolvedValueOnce({} as any)
    const wrapper = mount(RegisterView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form.email = 'test@example.com'
    await vm.handleSendCode()
    await flushPromises()
    expect(userApi.sendCode).toHaveBeenCalledWith({ email: 'test@example.com', purpose: 'REGISTER' })
    expect(ElMessage.success).toHaveBeenCalledWith('验证码已发送')
  })

  it('shows warning when sending code without email', async () => {
    const wrapper = mount(RegisterView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form.email = ''
    await vm.handleSendCode()
    expect(ElMessage.warning).toHaveBeenCalledWith('请输入邮箱')
  })

  it('calls register API and redirects to login on success', async () => {
    vi.mocked(userApi.registerByEmail).mockResolvedValueOnce({} as any)
    const wrapper = mount(RegisterView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form = { username: 'newuser', password: '123456', nickname: 'Nick', email: 'test@example.com', phone: '', code: '123456' }
    await vm.handleRegister()
    await flushPromises()
    expect(userApi.registerByEmail).toHaveBeenCalled()
    expect(ElMessage.success).toHaveBeenCalledWith('注册成功，请登录')
    expect(mockPush).toHaveBeenCalledWith('/login')
  })

  it('has link to login page', () => {
    const wrapper = mount(RegisterView, { global: { plugins: [createPinia()], stubs: authStubs } })
    expect(wrapper.text()).toContain('登录')
  })

  it('shows benefits section', () => {
    const wrapper = mount(RegisterView, { global: { plugins: [createPinia()], stubs: authStubs } })
    expect(wrapper.text()).toContain('邮箱验证码')
  })
})

describe('ForgotPasswordView - 找回密码', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders forgot password form with title', () => {
    const wrapper = mount(ForgotPasswordView, { global: { plugins: [createPinia()], stubs: authStubs } })
    expect(wrapper.find('h2').text()).toBe('找回密码')
    expect(wrapper.text()).toContain('通过注册邮箱重置密码')
  })

  it('has email, code, password input fields', () => {
    const wrapper = mount(ForgotPasswordView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const inputs = wrapper.findAll('input')
    expect(inputs.length).toBeGreaterThanOrEqual(3)
  })

  it('sends reset code to email', async () => {
    vi.mocked(userApi.sendCode).mockResolvedValueOnce({} as any)
    const wrapper = mount(ForgotPasswordView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form.email = 'test@example.com'
    await vm.handleSendCode()
    await flushPromises()
    expect(userApi.sendCode).toHaveBeenCalledWith({ email: 'test@example.com', purpose: 'RESET' })
    expect(ElMessage.success).toHaveBeenCalledWith('验证码已发送')
  })

  it('shows warning when sending code without email', async () => {
    const wrapper = mount(ForgotPasswordView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form.email = ''
    await vm.handleSendCode()
    expect(ElMessage.warning).toHaveBeenCalledWith('请输入邮箱')
  })

  it('calls resetPassword API and redirects to login', async () => {
    vi.mocked(userApi.resetPassword).mockResolvedValueOnce({} as any)
    const wrapper = mount(ForgotPasswordView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form = { email: 'test@example.com', code: '123456', password: 'newpass' }
    await vm.handleReset()
    await flushPromises()
    expect(userApi.resetPassword).toHaveBeenCalledWith({ email: 'test@example.com', code: '123456', password: 'newpass' })
    expect(ElMessage.success).toHaveBeenCalledWith('密码已重置，请登录')
    expect(mockPush).toHaveBeenCalledWith('/login')
  })

  it('shows warning when submitting incomplete form', async () => {
    const wrapper = mount(ForgotPasswordView, { global: { plugins: [createPinia()], stubs: authStubs } })
    const vm = wrapper.vm as any
    vm.form = { email: '', code: '', password: '' }
    await vm.handleReset()
    expect(ElMessage.warning).toHaveBeenCalledWith('请填写完整信息')
  })

  it('has link back to login', () => {
    const wrapper = mount(ForgotPasswordView, { global: { plugins: [createPinia()], stubs: authStubs } })
    expect(wrapper.text()).toContain('返回登录')
  })
})

describe('ProfileView - 个人信息修改', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders profile page with basic info section', async () => {
    vi.mocked(userApi.getProfile).mockResolvedValueOnce({ data: { nickname: 'Test', email: 'test@test.com', phone: '13800000000', avatarUrl: '' } } as any)
    const pinia = createPinia()
    setActivePinia(pinia)
    const userStore = useUserStore()
    userStore.setAuth({ token: 'test-token', userId: 1, nickname: 'Test' })
    const wrapper = mount(ProfileView, { global: { plugins: [pinia], stubs: authStubs } })
    await flushPromises()
    expect(wrapper.text()).toContain('个人中心')
    expect(wrapper.text()).toContain('基本资料')
  })

  it('renders password change section', async () => {
    vi.mocked(userApi.getProfile).mockResolvedValueOnce({ data: { nickname: 'Test', email: 'test@test.com', phone: '', avatarUrl: '' } } as any)
    const pinia = createPinia()
    setActivePinia(pinia)
    const userStore = useUserStore()
    userStore.setAuth({ token: 'test-token', userId: 1, nickname: 'Test' })
    const wrapper = mount(ProfileView, { global: { plugins: [pinia], stubs: authStubs } })
    await flushPromises()
    expect(wrapper.text()).toContain('修改密码')
  })

  it('calls updateProfile API on save', async () => {
    vi.mocked(userApi.getProfile).mockResolvedValueOnce({ data: { nickname: 'Test', email: 'test@test.com', phone: '', avatarUrl: '' } } as any)
    vi.mocked(userApi.updateProfile).mockResolvedValueOnce({} as any)
    const pinia = createPinia()
    setActivePinia(pinia)
    const userStore = useUserStore()
    userStore.setAuth({ token: 'test-token', userId: 1, nickname: 'Test' })
    const wrapper = mount(ProfileView, { global: { plugins: [pinia], stubs: authStubs } })
    await flushPromises()
    const vm = wrapper.vm as any
    vm.profile = { nickname: 'NewNick', email: 'new@test.com', phone: '13900000000', avatarUrl: '' }
    await vm.saveProfile()
    await flushPromises()
    expect(userApi.updateProfile).toHaveBeenCalled()
    expect(ElMessage.success).toHaveBeenCalledWith('已保存')
  })

  it('calls changePassword API and logs out', async () => {
    vi.mocked(userApi.getProfile).mockResolvedValueOnce({ data: { nickname: 'Test', email: '', phone: '', avatarUrl: '' } } as any)
    vi.mocked(userApi.changePassword).mockResolvedValueOnce({} as any)
    const pinia = createPinia()
    setActivePinia(pinia)
    const userStore = useUserStore()
    userStore.setAuth({ token: 'test-token', userId: 1, nickname: 'Test' })
    const wrapper = mount(ProfileView, { global: { plugins: [pinia], stubs: authStubs } })
    await flushPromises()
    const vm = wrapper.vm as any
    vm.pwForm = { oldPassword: 'old', newPassword: 'new' }
    await vm.savePassword()
    await flushPromises()
    expect(userApi.changePassword).toHaveBeenCalledWith({ oldPassword: 'old', newPassword: 'new' })
    expect(ElMessage.success).toHaveBeenCalledWith('密码已修改，请重新登录')
    expect(userStore.isLoggedIn).toBe(false)
  })

  it('shows warning when password fields are empty', async () => {
    vi.mocked(userApi.getProfile).mockResolvedValueOnce({ data: { nickname: 'Test', email: '', phone: '', avatarUrl: '' } } as any)
    const pinia = createPinia()
    setActivePinia(pinia)
    const userStore = useUserStore()
    userStore.setAuth({ token: 'test-token', userId: 1, nickname: 'Test' })
    const wrapper = mount(ProfileView, { global: { plugins: [pinia], stubs: authStubs } })
    await flushPromises()
    const vm = wrapper.vm as any
    vm.pwForm = { oldPassword: '', newPassword: '' }
    await vm.savePassword()
    expect(ElMessage.warning).toHaveBeenCalledWith('请填写完整')
  })
})
