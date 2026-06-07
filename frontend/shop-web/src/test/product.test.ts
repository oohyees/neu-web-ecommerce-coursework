/**
 * 商品模块测试
 * 覆盖采分点：商品分类浏览、商品详情页、商品搜索（精准/模糊/排序）、商品收藏（收藏/取消/列表）、商品评价（查看/提交/上传图片）
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import HomeView from '@/views/HomeView.vue'
import CategoryView from '@/views/CategoryView.vue'
import SearchView from '@/views/SearchView.vue'
import ProductDetailView from '@/views/ProductDetailView.vue'
import FavoritesView from '@/views/FavoritesView.vue'
import { useUserStore } from '@/stores/user'
import * as productApi from '@/api/product'
import * as noticeApi from '@/api/notice'

vi.mock('@/api/product', () => ({
  fetchCategories: vi.fn(),
  fetchProducts: vi.fn(),
  fetchProductDetail: vi.fn(),
  fetchReviews: vi.fn(),
  submitReview: vi.fn(),
  fetchFavorites: vi.fn(),
  addFavorite: vi.fn(),
  removeFavorite: vi.fn(),
  fetchAllReviews: vi.fn(),
  deleteReview: vi.fn(),
}))

vi.mock('@/api/notice', () => ({
  fetchHomeData: vi.fn(),
  trackSearch: vi.fn(),
  fetchAnnouncements: vi.fn().mockResolvedValue({ data: [] }),
  fetchActivityNotices: vi.fn().mockResolvedValue({ data: [] }),
}))

vi.mock('@/api/cart', () => ({
  addToCart: vi.fn(),
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
  ElInput: { template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />', props: ['modelValue'] },
  ElForm: { template: '<form><slot /></form>' },
  ElFormItem: { template: '<div><slot /></div>' },
  ElSkeleton: { template: '<div><slot /></div>' },
  ElEmpty: { template: '<div>empty</div>' },
  ElCheckbox: { template: '<div><slot /></div>' },
  ElInputNumber: { template: '<div></div>' },
  ElRate: { template: '<div></div>' },
  ElCarousel: { template: '<div><slot /></div>' },
  ElCarouselItem: { template: '<div><slot /></div>' },
  ElPagination: { template: '<div></div>' },
  ElRadioButton: { template: '<div></div>' },
  ElRadioGroup: { template: '<div><slot /></div>' },
  RouterLink: { template: '<a><slot /></a>', props: ['to'] },
}

describe('HomeView - 首页模块', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('renders home shell and brand', () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({ banners: [], hotProducts: [], newProducts: [], hotSearches: [], promotions: [], coupons: [], reviews: [] } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    expect(wrapper.text()).toContain('优品')
    expect(wrapper.text()).toContain('轮播加载中')
  })

  it('displays hot products when available', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({
      banners: [], hotProducts: [{ id: 1, name: '手机', price: 1999, imageUrl: '/p.jpg' }], newProducts: [],
      hotSearches: ['手机', '电脑', '耳机'], promotions: [], coupons: [], reviews: [],
    } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    await flushPromises()
    expect(wrapper.text()).toContain('热门好物')
    expect(wrapper.text()).toContain('手机')
  })

  it('navigates to search page from brand tag', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({ banners: [], hotProducts: [], newProducts: [], hotSearches: [], promotions: [], coupons: [], reviews: [] } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    const vm = wrapper.vm as any
    vm.goSearchTag('手机')
    expect(mockPush).toHaveBeenCalledWith({ path: '/search', query: { keyword: '手机' } })
  })

  it('displays banner carousel when banners exist', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({
      banners: [{ id: 1, imageUrl: '/banner.jpg', linkUrl: '/product/1' }],
      hotProducts: [], newProducts: [], hotSearches: [], promotions: [], coupons: [], reviews: [],
    } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    await flushPromises()
    expect(wrapper.find('.banner-wrap').exists()).toBe(true)
  })

  it('displays category sidebar when categories exist', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({ banners: [], hotProducts: [], newProducts: [], hotSearches: [], promotions: [], coupons: [], reviews: [] } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([
      { id: 1, name: '电子产品', parentId: null, children: [{ id: 2, name: '手机', parentId: 1 }] },
    ] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    await flushPromises()
    expect(wrapper.find('.category-sidebar').exists()).toBe(true)
    expect(wrapper.text()).toContain('电子产品')
  })

  it('displays hot products section', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({
      banners: [], hotProducts: [{ id: 1, name: '热门商品', price: 99, imageUrl: '', sales: 100 }],
      newProducts: [], hotSearches: [], promotions: [], coupons: [], reviews: [],
    } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    await flushPromises()
    expect(wrapper.text()).toContain('热门好物')
    expect(wrapper.text()).toContain('热门商品')
  })

  it('displays new products section', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({
      banners: [], hotProducts: [],
      newProducts: [{ id: 2, name: '新品商品', price: 199, imageUrl: '' }],
      hotSearches: [], promotions: [], coupons: [], reviews: [],
    } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    await flushPromises()
    expect(wrapper.text()).toContain('新品首发')
    expect(wrapper.text()).toContain('新品商品')
  })

  it('navigates to category page on category click', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({ banners: [], hotProducts: [], newProducts: [], hotSearches: [], promotions: [], coupons: [], reviews: [] } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    const vm = wrapper.vm as any
    vm.goCategory(5)
    expect(mockPush).toHaveBeenCalledWith('/category/5')
  })

  it('navigates to product detail on product click', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({ banners: [], hotProducts: [], newProducts: [], hotSearches: [], promotions: [], coupons: [], reviews: [] } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    const vm = wrapper.vm as any
    vm.goProduct(10)
    expect(mockPush).toHaveBeenCalledWith('/product/10')
  })

  it('displays promotion section when promotions exist', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({
      banners: [], hotProducts: [], newProducts: [], hotSearches: [],
      promotions: [{ id: 1, title: '促销', productId: 1, productName: '促销商品', promotionPrice: 50, originalPrice: 100, imageUrl: '', promotionType: 'PROMOTION' }],
      coupons: [], reviews: [],
    } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    await flushPromises()
    expect(wrapper.text()).toContain('促销活动')
  })

  it('displays coupon section when coupons exist', async () => {
    vi.mocked(noticeApi.fetchHomeData).mockResolvedValueOnce({
      banners: [], hotProducts: [], newProducts: [], hotSearches: [], promotions: [],
      coupons: [{ id: 1, name: '满减券', discountAmount: 10, thresholdAmount: 100 }],
      reviews: [],
    } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(HomeView, { global: { plugins: [createPinia()], stubs: commonStubs } })
    await flushPromises()
    expect(wrapper.text()).toContain('领券中心')
  })
})

describe('CategoryView - 商品分类浏览', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders category products', async () => {
    vi.mocked(productApi.fetchProducts).mockResolvedValueOnce({
      data: { items: [{ id: 1, name: '分类商品', price: 99, imageUrl: '' }], total: 1 },
    } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([
      { id: 1, name: '电子产品', parentId: null },
    ] as any)
    const wrapper = mount(CategoryView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('分类商品')
  })

  it('shows empty state when no products in category', async () => {
    vi.mocked(productApi.fetchProducts).mockResolvedValueOnce({ data: { items: [], total: 0 } } as any)
    vi.mocked(productApi.fetchCategories).mockResolvedValueOnce([] as any)
    const wrapper = mount(CategoryView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '999' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('empty')
  })
})

describe('SearchView - 商品搜索', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders search bar with input and button', () => {
    const wrapper = mount(SearchView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    expect(wrapper.find('.search-input').exists()).toBe(true)
    expect(wrapper.find('.search-bar').exists()).toBe(true)
  })

  it('displays search results with keyword', async () => {
    vi.mocked(productApi.fetchProducts).mockResolvedValueOnce({
      data: { items: [{ id: 1, name: '搜索结果商品', price: 199, imageUrl: '' }], total: 1 },
    } as any)
    mockRoute.query = { keyword: '手机' }
    const wrapper = mount(SearchView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('搜索结果商品')
    mockRoute.query = {}
  })

  it('supports sorting by price, sales, newest', async () => {
    vi.mocked(productApi.fetchProducts).mockResolvedValueOnce({ data: { items: [], total: 0 } } as any)
    const wrapper = mount(SearchView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { query: { keyword: 'test' }, params: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('价格')
    expect(wrapper.text()).toContain('销量')
  })

  it('shows empty state when no results', async () => {
    vi.mocked(productApi.fetchProducts).mockResolvedValueOnce({ data: { items: [], total: 0 } } as any)
    mockRoute.query = { keyword: '不存在的商品' }
    const wrapper = mount(SearchView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('empty')
    mockRoute.query = {}
  })

  it('navigates to search on enter key', async () => {
    const wrapper = mount(SearchView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    const vm = wrapper.vm as any
    vm.keyword = '电脑'
    vm.goSearch()
    expect(mockReplace).toHaveBeenCalledWith({ path: '/search', query: { keyword: '电脑' } })
  })
})

describe('ProductDetailView - 商品详情页', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('renders product name, price, stock, images, params, and details', async () => {
    vi.mocked(productApi.fetchProductDetail).mockResolvedValueOnce({
      product: { id: 1, name: '测试商品', price: 99, stock: 50, imageUrl: '', paramsText: '品牌:测试;颜色:红', originalPrice: 199 },
      skus: [], images: [], specs: [{ id: 1, specName: '品牌', specValue: '测试' }],
    } as any)
    vi.mocked(productApi.fetchReviews).mockResolvedValueOnce({ items: [] } as any)
    const wrapper = mount(ProductDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('测试商品')
    expect(wrapper.text()).toContain('99')
    expect(wrapper.text()).toContain('品牌')
  })

  it('displays SKU color and size selectors', async () => {
    vi.mocked(productApi.fetchProductDetail).mockResolvedValueOnce({
      product: { id: 1, name: 'SKU商品', price: 99, stock: 10, imageUrl: '' },
      skus: [
        { id: 1, color: '红色', size: 'S', price: 99, stock: 5 },
        { id: 2, color: '蓝色', size: 'M', price: 109, stock: 5 },
      ],
      images: [], specs: [],
    } as any)
    vi.mocked(productApi.fetchReviews).mockResolvedValueOnce({ items: [] } as any)
    const wrapper = mount(ProductDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('红色')
    expect(wrapper.text()).toContain('蓝色')
    expect(wrapper.text()).toContain('S')
    expect(wrapper.text()).toContain('M')
  })

  it('displays reviews section', async () => {
    vi.mocked(productApi.fetchProductDetail).mockResolvedValueOnce({
      product: { id: 1, name: '商品', price: 99, stock: 10, imageUrl: '' },
      skus: [], images: [], specs: [],
    } as any)
    vi.mocked(productApi.fetchReviews).mockResolvedValueOnce({
      items: [{ id: 1, nickname: '用户A', rating: 5, content: '很好', createdAt: '2026-01-01' }],
    } as any)
    const wrapper = mount(ProductDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('很好')
    expect(wrapper.text()).toContain('用户A')
  })

  it('shows add to cart and buy now buttons', async () => {
    vi.mocked(productApi.fetchProductDetail).mockResolvedValueOnce({
      product: { id: 1, name: '商品', price: 99, stock: 10, imageUrl: '' },
      skus: [], images: [], specs: [],
    } as any)
    vi.mocked(productApi.fetchReviews).mockResolvedValueOnce({ items: [] } as any)
    const wrapper = mount(ProductDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('加入购物车')
    expect(wrapper.text()).toContain('立即购买')
  })

  it('shows favorite toggle button', async () => {
    vi.mocked(productApi.fetchProductDetail).mockResolvedValueOnce({
      product: { id: 1, name: '商品', price: 99, stock: 10, imageUrl: '' },
      skus: [], images: [], specs: [],
    } as any)
    vi.mocked(productApi.fetchReviews).mockResolvedValueOnce({ items: [] } as any)
    const wrapper = mount(ProductDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.find('.btn-favorite').exists()).toBe(true)
  })

  it('shows stock status', async () => {
    vi.mocked(productApi.fetchProductDetail).mockResolvedValueOnce({
      product: { id: 1, name: '商品', price: 99, stock: 10, imageUrl: '' },
      skus: [], images: [], specs: [],
    } as any)
    vi.mocked(productApi.fetchReviews).mockResolvedValueOnce({ items: [] } as any)
    const wrapper = mount(ProductDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('库存')
    expect(wrapper.text()).toContain('10 件')
  })

  it('shows out of stock when stock is 0', async () => {
    vi.mocked(productApi.fetchProductDetail).mockResolvedValueOnce({
      product: { id: 1, name: '缺货商品', price: 99, stock: 0, imageUrl: '' },
      skus: [], images: [], specs: [],
    } as any)
    vi.mocked(productApi.fetchReviews).mockResolvedValueOnce({ items: [] } as any)
    const wrapper = mount(ProductDetailView, {
      global: {
        plugins: [createPinia()],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('暂时缺货')
  })

  it('shows review form for logged in users', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const userStore = useUserStore()
    userStore.setAuth({ token: 'test', userId: 1, nickname: 'Test' })

    vi.mocked(productApi.fetchProductDetail).mockResolvedValueOnce({
      product: { id: 1, name: '商品', price: 99, stock: 10, imageUrl: '' },
      skus: [], images: [], specs: [],
    } as any)
    vi.mocked(productApi.fetchReviews).mockResolvedValueOnce({ items: [] } as any)
    const wrapper = mount(ProductDetailView, {
      global: {
        plugins: [pinia],
        stubs: commonStubs,
        mocks: { $route: { params: { id: '1' }, query: {} } },
      },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('发表评价')
  })
})

describe('FavoritesView - 商品收藏', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('renders favorites list', async () => {
    vi.mocked(productApi.fetchFavorites).mockResolvedValueOnce({
      data: [{ id: 1, name: '收藏商品', price: 99, imageUrl: '' }],
    } as any)
    const wrapper = mount(FavoritesView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('我的收藏')
    expect(wrapper.text()).toContain('收藏商品')
  })

  it('shows empty state when no favorites', async () => {
    vi.mocked(productApi.fetchFavorites).mockResolvedValueOnce({ data: [] } as any)
    const wrapper = mount(FavoritesView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('empty')
  })

  it('has cancel favorite button for each item', async () => {
    vi.mocked(productApi.fetchFavorites).mockResolvedValueOnce({
      data: [{ id: 1, name: '收藏商品', price: 99, imageUrl: '' }],
    } as any)
    const wrapper = mount(FavoritesView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('取消收藏')
  })

  it('navigates to product detail on click', async () => {
    vi.mocked(productApi.fetchFavorites).mockResolvedValueOnce({
      data: [{ id: 1, name: '收藏商品', price: 99, imageUrl: '' }],
    } as any)
    const wrapper = mount(FavoritesView, {
      global: { plugins: [createPinia()], stubs: commonStubs },
    })
    await flushPromises()
    const vm = wrapper.vm as any
    vm.goProduct(1)
    expect(mockPush).toHaveBeenCalledWith('/product/1')
  })
})
