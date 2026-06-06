import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CartItem } from '../types/index'
import { useUserStore } from './user'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])
  const loading = ref(false)
  const count = computed(() => items.value.length)
  const selectedItems = computed(() => items.value.filter((i) => i.selected))
  const totalPrice = computed(() => selectedItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0))
  const allSelected = computed(() => items.value.length > 0 && items.value.every((i) => i.selected))

  async function fetch() {
    const userStore = useUserStore()
    if (!userStore.userId) return
    loading.value = true
    try {
      const { api } = await import('../api/index')
      const res = await api.get('/cart', { params: { userId: userStore.userId } })
      items.value = (res.data.data || []).map((i: any) => ({ ...i, selected: i.selected ?? true }))
    } finally { loading.value = false }
  }

  async function add(productId: number, quantity = 1) {
    const userStore = useUserStore()
    if (!userStore.userId) return
    const { api } = await import('../api/index')
    await api.post('/cart/items', { userId: userStore.userId, productId, quantity })
    await fetch()
  }

  async function updateQuantity(itemId: number, quantity: number) {
    if (quantity < 1) return
    const { api } = await import('../api/index')
    await api.put(`/cart/items/${itemId}`, { quantity })
    const item = items.value.find((i) => i.id === itemId)
    if (item) item.quantity = quantity
  }

  async function remove(itemId: number) {
    const { api } = await import('../api/index')
    await api.delete(`/cart/items/${itemId}`)
    items.value = items.value.filter((i) => i.id !== itemId)
  }

  function toggleSelect(itemId: number) {
    const item = items.value.find((i) => i.id === itemId)
    if (item) item.selected = !item.selected
  }

  function clear() { items.value = [] }

  return { items, loading, count, selectedItems, totalPrice, allSelected, fetch, add, updateQuantity, remove, toggleSelect, clear }
})
