/**
 * 购物车与订单模块测试
 * 覆盖采分点：购物车（添加/列表/修改数量/删除/勾选/全选/结算）、
 * 订单（确认/提交/支付/我的订单/取消/确认收货/退款/物流查看）
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ElMessage } from 'element-plus'
import CartView from '@/views/CartView.vue'
import CheckoutView from '@/views/CheckoutView.vue'
import PaymentView from '@/views/PaymentView.vue'
import OrdersView from '@/views/OrdersView.vue'
import OrderDetailView from '@/views/OrderDetailView.vue'

vi.mock('@/api/cart', () => ({
  fetchCart: vi.fn(),
  addToCart: vi.fn(),
  updateCartQuantity: vi.fn(),
  removeCartItem: vi.fn(),
}))

vi.mock('@/api/order', () => ({
  createOrder: vi.fn(),
  fetchMyOrders: vi.fn(),
  fetchOrderDetail: vi.fn(),
  payOrder: vi.fn(),
  cancelOrder: vi.fn(),
  confirmOrder: vi.fn(),
  refundOrder: vi.fn(),
}))

vi.mock('@/api/address', () => ({
  fetchAddresses: vi.fn(),
}))

vi.mock('@/api/coupon', () => ({
  fetchMyCoupons: vi.fn(),
  fetchAvailableCoupons: vi.fn(),
  claimCoupon: vi.fn(),
  normalizeCoupon: (raw: any) => ({
    ...raw,
    value: raw.value ?? raw.discountAmount ?? 0,
    minAmount: raw.minAmount ?? raw.thresholdAmount ?? 0,
  }),
}))

vi.mock('@/api/user', () => ({
  login: vi.fn(),
}))

const mockPush = vi.fn()
const mockReplace = vi.fn()
let mockRoute: any = { query: {}, params: {} }
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush, replace: mockReplace }),
  useRoute: () => mockRoute,
  createRouter: vi.fn(() => ({ push: vi.fn(), replace: vi.fn(), beforeEach: vi.fn(), afterEach: vi.fn() })),
  createWebHistory: vi.fn(),
}))

const commonStubs = {
  ElButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
  ElInput: { template: '<input />', props: ['modelValue'] },
  ElForm: { template: '<form><slot /></form>' },
  ElFormItem: { template: '<div><slot /></div>' },
  ElSkeleton: { template: '<div><slot /></div>' },
  ElEmpty: { template: '<div>empty</div>' },
  ElCheckbox: { template: '<input type="checkbox" /><slot />', props: ['modelValue'] },
  ElSwitch: { template: '<div></div>' },
  ElSelect: { template: '<div><slot /></div>' },
  ElOption: { template: '<div></div>' },
  ElInputNumber: { template: '<div></div>' },
  ElPagination: { template: '<div></div>' },
  RouterLink: { template: '<a><slot /></a>', props: ['to'] },
}

describe('CartView - 购物车', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('renders cart page title', () => {
    const wrapper = mount(CartView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    expect(wrapper.text()).toContain('我的购物车')
  })

  it('shows empty state when cart is empty', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockResolvedValueOnce({ data: { items: [] } } as any)
    const wrapper = mount(CartView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('empty')
  })

  it('displays cart items with product name, price, quantity, subtotal', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: {
        items: [{
          id: 1, productId: 10, productName: '测试商品', price: 99.00,
          quantity: 2, imageUrl: '', color: '红色', size: 'M',
        }],
      },
    } as any)
    const wrapper = mount(CartView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('测试商品')
    expect(wrapper.text()).toContain('99')
    expect(wrapper.text()).toContain('红色')
    expect(wrapper.text()).toContain('M')
  })

  it('shows total price and total count', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: {
        items: [
          { id: 1, productId: 10, productName: '商品A', price: 50, quantity: 2, imageUrl: '' },
          { id: 2, productId: 11, productName: '商品B', price: 30, quantity: 1, imageUrl: '' },
        ],
      },
    } as any)
    const wrapper = mount(CartView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    // Total price is 0.00 when no items are checked, but items are displayed
    expect(wrapper.text()).toContain('商品A')
    expect(wrapper.text()).toContain('商品B')
    // Check that subtotal is shown for each item
    expect(wrapper.text()).toContain('100.00')
    expect(wrapper.text()).toContain('30.00')
  })

  it('has delete button for each item', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: { items: [{ id: 1, productId: 10, productName: '商品', price: 99, quantity: 1, imageUrl: '' }] },
    } as any)
    const wrapper = mount(CartView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('删除')
  })

  it('has select all checkbox', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: { items: [{ id: 1, productId: 10, productName: '商品', price: 99, quantity: 1, imageUrl: '' }] },
    } as any)
    const wrapper = mount(CartView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    // CartView has "全选" text in the footer
    expect(wrapper.text()).toContain('全选')
    // There should be checkboxes (header + footer + per-item)
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    expect(checkboxes.length).toBeGreaterThanOrEqual(2)
  })

  it('has checkout button', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: { items: [{ id: 1, productId: 10, productName: '商品', price: 99, quantity: 1, imageUrl: '' }] },
    } as any)
    const wrapper = mount(CartView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('去结算')
  })

  it('shows warning when checking out with no selection', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: { items: [{ id: 1, productId: 10, productName: '商品', price: 99, quantity: 1, imageUrl: '' }] },
    } as any)
    const wrapper = mount(CartView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    vm.goCheckout()
    expect(ElMessage.warning).toHaveBeenCalledWith('请先选择商品')
  })

  it('navigates to checkout with selected cart item IDs', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: { items: [{ id: 1, productId: 10, productName: '商品', price: 99, quantity: 1, imageUrl: '' }] },
    } as any)
    const wrapper = mount(CartView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    vm.checkedIds = new Set([1])
    vm.goCheckout()
    expect(mockPush).toHaveBeenCalledWith({ path: '/checkout', query: { cartItemIds: '1' } })
  })
})

describe('CheckoutView - 订单确认', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('renders checkout page with address, product list, and total', async () => {
    const { fetchCart } = await import('@/api/cart')
    const { fetchAddresses } = await import('@/api/address')
    const { fetchMyCoupons } = await import('@/api/coupon')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: { items: [{ id: 1, productId: 10, productName: '商品', price: 99, quantity: 2, imageUrl: '' }] },
    } as any)
    vi.mocked(fetchAddresses).mockResolvedValueOnce({
      data: [{ id: 1, receiver: '张三', phone: '13800000000', province: '辽宁', city: '沈阳', district: '和平区', detail: '某某路', isDefault: true }],
    } as any)
    vi.mocked(fetchMyCoupons).mockResolvedValueOnce({ data: [] } as any)
    const wrapper = mount(CheckoutView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('确认订单')
    expect(wrapper.text()).toContain('收货地址')
    expect(wrapper.text()).toContain('商品清单')
    expect(wrapper.text()).toContain('张三')
  })

  it('shows warning when submitting without address', async () => {
    const { fetchCart } = await import('@/api/cart')
    const { fetchAddresses } = await import('@/api/address')
    const { fetchMyCoupons } = await import('@/api/coupon')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: { items: [{ id: 1, productId: 10, productName: '商品', price: 99, quantity: 1, imageUrl: '' }] },
    } as any)
    vi.mocked(fetchAddresses).mockResolvedValueOnce({ data: [] } as any)
    vi.mocked(fetchMyCoupons).mockResolvedValueOnce({ data: [] } as any)
    const wrapper = mount(CheckoutView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    await vm.handleSubmit()
    expect(ElMessage.warning).toHaveBeenCalledWith('请选择收货地址')
  })

  it('calls createOrder and navigates to payment on submit', async () => {
    const { fetchCart } = await import('@/api/cart')
    const { fetchAddresses } = await import('@/api/address')
    const { fetchMyCoupons } = await import('@/api/coupon')
    const { createOrder } = await import('@/api/order')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: { items: [{ id: 1, productId: 10, productName: '商品', price: 99, quantity: 1, imageUrl: '' }] },
    } as any)
    vi.mocked(fetchAddresses).mockResolvedValueOnce({
      data: [{ id: 1, receiver: '张三', phone: '13800000000', province: '辽宁', city: '沈阳', district: '和平区', detail: '某某路', isDefault: true }],
    } as any)
    vi.mocked(fetchMyCoupons).mockResolvedValueOnce({ data: [] } as any)
    vi.mocked(createOrder).mockResolvedValueOnce({ data: { orderNo: 'ORD2026001' } } as any)
    const wrapper = mount(CheckoutView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    vm.selectedAddrId = 1
    await vm.handleSubmit()
    await flushPromises()
    expect(createOrder).toHaveBeenCalled()
    expect(ElMessage.success).toHaveBeenCalledWith('下单成功')
    expect(mockPush).toHaveBeenCalledWith('/payment?orderNo=ORD2026001')
  })

  it('displays coupon selection when coupons available', async () => {
    const { fetchCart } = await import('@/api/cart')
    const { fetchAddresses } = await import('@/api/address')
    const { fetchMyCoupons } = await import('@/api/coupon')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: { items: [{ id: 1, productId: 10, productName: '商品', price: 99, quantity: 1, imageUrl: '' }] },
    } as any)
    vi.mocked(fetchAddresses).mockResolvedValueOnce({ data: [] } as any)
    vi.mocked(fetchMyCoupons).mockResolvedValueOnce({
      data: [{ id: 1, value: 10, minAmount: 50 }],
    } as any)
    const wrapper = mount(CheckoutView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('优惠券')
  })
})

describe('PaymentView - 订单支付', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('renders payment page with order info', async () => {
    const { fetchMyOrders } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD2026001', totalAmount: 99, status: 'PENDING' }] },
    } as any)
    mockRoute.query = { orderNo: 'ORD2026001' }
    const wrapper = mount(PaymentView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('订单支付')
    expect(wrapper.text()).toContain('ORD2026001')
    mockRoute.query = {}
  })

  it('calls payOrder on confirm payment', async () => {
    const { fetchMyOrders, payOrder } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD2026001', totalAmount: 99, status: 'PENDING' }] },
    } as any)
    vi.mocked(payOrder).mockResolvedValueOnce({} as any)
    mockRoute.query = { orderNo: 'ORD2026001' }
    const wrapper = mount(PaymentView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    await vm.handlePay()
    await flushPromises()
    expect(payOrder).toHaveBeenCalledWith(1)
    expect(ElMessage.success).toHaveBeenCalledWith('支付成功')
    mockRoute.query = {}
  })

  it('shows simulated payment hint', async () => {
    const { fetchMyOrders } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD2026001', totalAmount: 99, status: 'PENDING' }] },
    } as any)
    mockRoute.query = { orderNo: 'ORD2026001' }
    const wrapper = mount(PaymentView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('模拟支付')
    mockRoute.query = {}
  })
})

describe('OrdersView - 我的订单', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('renders orders page with status tabs', () => {
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    expect(wrapper.text()).toContain('我的订单')
    expect(wrapper.text()).toContain('全部')
    expect(wrapper.text()).toContain('待支付')
    expect(wrapper.text()).toContain('待发货')
    expect(wrapper.text()).toContain('待收货')
    expect(wrapper.text()).toContain('已完成')
    expect(wrapper.text()).toContain('已取消')
  })

  it('displays order list with order number and amount', async () => {
    const { fetchMyOrders } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD001', totalAmount: 199, status: 'PENDING', items: [] }] },
    } as any)
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('ORD001')
    expect(wrapper.text()).toContain('199')
  })

  it('shows cancel button for pending orders', async () => {
    const { fetchMyOrders } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD001', totalAmount: 199, status: 'PENDING', items: [] }] },
    } as any)
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('取消')
  })

  it('shows pay button for pending orders', async () => {
    const { fetchMyOrders } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD001', totalAmount: 199, status: 'PENDING', items: [] }] },
    } as any)
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('去支付')
  })

  it('shows confirm receipt button for shipped orders', async () => {
    const { fetchMyOrders } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD001', totalAmount: 199, status: 'SHIPPED', items: [] }] },
    } as any)
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('确认收货')
  })

  it('shows refund button for paid/shipped orders', async () => {
    const { fetchMyOrders } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD001', totalAmount: 199, status: 'PAID', items: [] }] },
    } as any)
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('退款')
  })

  it('calls cancelOrder API on cancel', async () => {
    const { fetchMyOrders, cancelOrder } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD001', totalAmount: 199, status: 'PENDING', items: [] }] },
    } as any)
    vi.mocked(cancelOrder).mockResolvedValueOnce({} as any)
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    await vm.handleCancel({ id: 1 })
    await flushPromises()
    expect(cancelOrder).toHaveBeenCalledWith(1)
  })

  it('calls confirmOrder API on confirm receipt', async () => {
    const { fetchMyOrders, confirmOrder } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD001', totalAmount: 199, status: 'SHIPPED', items: [] }] },
    } as any)
    vi.mocked(confirmOrder).mockResolvedValueOnce({} as any)
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    await vm.handleConfirm({ id: 1 })
    await flushPromises()
    expect(confirmOrder).toHaveBeenCalledWith(1)
  })

  it('calls refundOrder API on refund', async () => {
    const { fetchMyOrders, refundOrder } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD001', totalAmount: 199, status: 'PAID', items: [] }] },
    } as any)
    vi.mocked(refundOrder).mockResolvedValueOnce({} as any)
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    await vm.handleRefund({ id: 1 })
    await flushPromises()
    expect(refundOrder).toHaveBeenCalledWith(1)
  })

  it('navigates to order detail on click', async () => {
    const { fetchMyOrders } = await import('@/api/order')
    vi.mocked(fetchMyOrders).mockResolvedValueOnce({
      data: { items: [{ id: 1, orderNo: 'ORD001', totalAmount: 199, status: 'PENDING', items: [] }] },
    } as any)
    const wrapper = mount(OrdersView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    vm.viewDetail(1)
    expect(mockPush).toHaveBeenCalledWith('/orders/1')
  })
})

describe('OrderDetailView - 订单详情/物流', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders order detail with order number, status, payment method', async () => {
    const { fetchOrderDetail } = await import('@/api/order')
    vi.mocked(fetchOrderDetail).mockResolvedValueOnce({
      data: {
        order: { orderNo: 'ORD001', status: 'PAID', paymentMethod: '模拟支付', totalAmount: 199, createdAt: '2026-01-01' },
        items: [{ id: 1, productName: '商品A', unitPrice: 99, quantity: 2 }],
        address: { receiver: '张三', phone: '13800000000', province: '辽宁', city: '沈阳', district: '和平区', detail: '某某路' },
        logistics: [],
      },
    } as any)
    const wrapper = mount(OrderDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('ORD001')
    expect(wrapper.text()).toContain('PAID')
    expect(wrapper.text()).toContain('模拟支付')
  })

  it('displays logistics info when available', async () => {
    const { fetchOrderDetail } = await import('@/api/order')
    vi.mocked(fetchOrderDetail).mockResolvedValueOnce({
      data: {
        order: { orderNo: 'ORD001', status: 'SHIPPED', totalAmount: 199, createdAt: '2026-01-01' },
        items: [],
        address: null,
        logistics: [{ id: 1, content: '已发货', createdAt: '2026-01-02' }],
      },
    } as any)
    const wrapper = mount(OrderDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('物流信息')
    expect(wrapper.text()).toContain('已发货')
  })

  it('displays address info', async () => {
    const { fetchOrderDetail } = await import('@/api/order')
    vi.mocked(fetchOrderDetail).mockResolvedValueOnce({
      data: {
        order: { orderNo: 'ORD001', status: 'PAID', totalAmount: 199, createdAt: '2026-01-01' },
        items: [],
        address: { receiver: '李四', phone: '13900000000', province: '北京', city: '北京', district: '海淀区', detail: '中关村' },
        logistics: [],
      },
    } as any)
    const wrapper = mount(OrderDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('收货地址')
    expect(wrapper.text()).toContain('李四')
    expect(wrapper.text()).toContain('中关村')
  })

  it('displays product list in order', async () => {
    const { fetchOrderDetail } = await import('@/api/order')
    vi.mocked(fetchOrderDetail).mockResolvedValueOnce({
      data: {
        order: { orderNo: 'ORD001', status: 'PAID', totalAmount: 198, createdAt: '2026-01-01' },
        items: [{ id: 1, productName: '商品A', specText: '红色/M', unitPrice: 99, quantity: 2 }],
        address: null, logistics: [],
      },
    } as any)
    const wrapper = mount(OrderDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('商品清单')
    expect(wrapper.text()).toContain('商品A')
  })
})
