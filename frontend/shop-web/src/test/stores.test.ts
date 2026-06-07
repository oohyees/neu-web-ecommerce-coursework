/**
 * Pinia Store 测试
 * 覆盖采分点：用户认证状态管理、购物车状态管理、收藏状态管理
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useFavoriteStore } from '@/stores/favorite'
import { markFreshLogin, consumeFreshLogin } from '@/utils/freshLogin'

// Mock API modules
vi.mock('@/api/cart', () => ({
  fetchCart: vi.fn(),
  addToCart: vi.fn(),
  updateCartQuantity: vi.fn(),
  removeCartItem: vi.fn(),
}))

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

vi.mock('@/utils/freshLogin', () => ({
  markFreshLogin: vi.fn(),
  consumeFreshLogin: vi.fn(() => false),
}))

describe('useUserStore - 用户状态管理', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initial state is not logged in', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.token).toBe('')
    expect(store.userId).toBeNull()
    expect(store.nickname).toBe('')
  })

  it('setAuth sets token, userId, nickname and marks fresh login', () => {
    const store = useUserStore()
    store.setAuth({ token: 'abc123', userId: 1, nickname: '测试用户' })
    expect(store.token).toBe('abc123')
    expect(store.userId).toBe(1)
    expect(store.nickname).toBe('测试用户')
    expect(store.isLoggedIn).toBe(true)
    expect(markFreshLogin).toHaveBeenCalled()
  })

  it('setProfile updates profile fields', () => {
    const store = useUserStore()
    store.setProfile({ nickname: '新昵称', email: 'new@test.com', phone: '13900000000', avatarUrl: '/avatar.jpg' })
    expect(store.nickname).toBe('新昵称')
    expect(store.email).toBe('new@test.com')
    expect(store.phone).toBe('13900000000')
    expect(store.avatarUrl).toBe('/avatar.jpg')
  })

  it('setProfile only updates provided fields', () => {
    const store = useUserStore()
    store.setAuth({ token: 'abc', userId: 1, nickname: '原昵称' })
    store.setProfile({ nickname: '改昵称' })
    expect(store.nickname).toBe('改昵称')
    expect(store.userId).toBe(1)
  })

  it('logout clears all auth state', () => {
    const store = useUserStore()
    store.setAuth({ token: 'abc', userId: 1, nickname: '用户' })
    store.setProfile({ email: 'test@test.com', phone: '13800000000', avatarUrl: '/a.jpg' })
    store.logout()
    expect(store.token).toBe('')
    expect(store.userId).toBeNull()
    expect(store.nickname).toBe('')
    expect(store.avatarUrl).toBe('')
    expect(store.email).toBe('')
    expect(store.phone).toBe('')
    expect(store.isLoggedIn).toBe(false)
    expect(consumeFreshLogin).toHaveBeenCalled()
  })

  it('isLoggedIn is computed from token', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
    store.setAuth({ token: 'xyz', userId: 2, nickname: 'User' })
    expect(store.isLoggedIn).toBe(true)
    store.logout()
    expect(store.isLoggedIn).toBe(false)
  })
})

describe('useCartStore - 购物车状态管理', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initial itemCount is 0', () => {
    const store = useCartStore()
    expect(store.itemCount).toBe(0)
  })

  it('refresh sets itemCount from cart items when logged in', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockResolvedValueOnce({
      data: {
        items: [
          { id: 1, quantity: 3 },
          { id: 2, quantity: 2 },
        ],
      },
    } as any)
    const userStore = useUserStore()
    userStore.setAuth({ token: 'abc', userId: 1, nickname: 'Test' })
    const store = useCartStore()
    await store.refresh()
    expect(store.itemCount).toBe(5)
  })

  it('refresh sets itemCount to 0 when not logged in', async () => {
    const store = useCartStore()
    await store.refresh()
    expect(store.itemCount).toBe(0)
  })

  it('refresh sets itemCount to 0 on API error', async () => {
    const { fetchCart } = await import('@/api/cart')
    vi.mocked(fetchCart).mockRejectedValueOnce(new Error('Network error'))
    const userStore = useUserStore()
    userStore.setAuth({ token: 'abc', userId: 1, nickname: 'Test' })
    const store = useCartStore()
    await store.refresh()
    expect(store.itemCount).toBe(0)
  })
})

describe('useFavoriteStore - 收藏状态管理', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initial state has empty ids set', () => {
    const store = useFavoriteStore()
    expect(store.ids.size).toBe(0)
    expect(store.loading).toBe(false)
  })

  it('isFavorite returns true for favorited products', () => {
    const store = useFavoriteStore()
    store.ids = new Set([1, 2, 3])
    expect(store.isFavorite(1)).toBe(true)
    expect(store.isFavorite(2)).toBe(true)
    expect(store.isFavorite(4)).toBe(false)
  })

  it('refresh loads favorite IDs from API', async () => {
    const { fetchFavorites } = await import('@/api/product')
    vi.mocked(fetchFavorites).mockResolvedValueOnce({
      data: [{ productId: 10 }, { productId: 20 }, { productId: 30 }],
    } as any)
    const store = useFavoriteStore()
    await store.refresh()
    expect(store.ids.size).toBe(3)
    expect(store.isFavorite(10)).toBe(true)
    expect(store.isFavorite(20)).toBe(true)
    expect(store.isFavorite(40)).toBe(false)
  })

  it('clear resets favorite IDs', () => {
    const store = useFavoriteStore()
    store.ids = new Set([1, 2, 3])
    store.clear()
    expect(store.ids.size).toBe(0)
  })

  it('toggle adds to favorites when not favorite', async () => {
    const { addFavorite } = await import('@/api/product')
    vi.mocked(addFavorite).mockResolvedValueOnce({} as any)
    const store = useFavoriteStore()
    store.ids = new Set()
    const result = await store.toggle(5)
    expect(result).toBe(true)
    expect(store.isFavorite(5)).toBe(true)
    expect(addFavorite).toHaveBeenCalledWith(5)
  })

  it('toggle removes from favorites when already favorite', async () => {
    const { removeFavorite } = await import('@/api/product')
    vi.mocked(removeFavorite).mockResolvedValueOnce({} as any)
    const store = useFavoriteStore()
    store.ids = new Set([5])
    const result = await store.toggle(5)
    expect(result).toBe(false)
    expect(store.isFavorite(5)).toBe(false)
    expect(removeFavorite).toHaveBeenCalledWith(5)
  })

  it('toggle rolls back on API error', async () => {
    const { addFavorite } = await import('@/api/product')
    vi.mocked(addFavorite).mockRejectedValueOnce(new Error('API error'))
    const store = useFavoriteStore()
    store.ids = new Set()
    await expect(store.toggle(5)).rejects.toThrow('操作失败')
    expect(store.isFavorite(5)).toBe(false)
  })

  it('refresh sets loading state', async () => {
    const { fetchFavorites } = await import('@/api/product')
    let resolvePromise: (v: any) => void
    vi.mocked(fetchFavorites).mockImplementationOnce(() => new Promise(r => { resolvePromise = r }))
    const store = useFavoriteStore()
    const promise = store.refresh()
    expect(store.loading).toBe(true)
    resolvePromise!({ data: [] })
    await promise
    expect(store.loading).toBe(false)
  })
})
