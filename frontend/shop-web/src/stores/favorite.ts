import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Favorite } from '../types/index'

export const useFavoriteStore = defineStore('favorite', () => {
  const items = ref<Favorite[]>([])
  const loading = ref(false)
  const count = ref(0)

  async function fetch() {
    const { api } = await import('../api/index')
    const res = await api.get('/favorites')
    items.value = res.data.data || []
    count.value = items.value.length
  }

  function isFavorited(productId: number): boolean {
    return items.value.some((f) => f.productId === productId)
  }

  function clear() { items.value = []; count.value = 0 }

  return { items, loading, count, fetch, isFavorited, clear }
})
