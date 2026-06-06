import { defineStore } from 'pinia'
import { ref } from 'vue'
import { addFavorite, removeFavorite, fetchFavorites } from '@/api/product'

export const useFavoriteStore = defineStore('favorite', () => {
  const ids = ref<Set<number>>(new Set())
  const loading = ref(false)

  function isFavorite(productId: number): boolean {
    return ids.value.has(productId)
  }

  async function refresh() {
    loading.value = true
    try {
      const res = await fetchFavorites()
      const list = Array.isArray(res.data) ? res.data : []
      ids.value = new Set(list.map((item: any) => item.productId ?? item.id))
    } catch {
      ids.value = new Set()
    } finally {
      loading.value = false
    }
  }

  function clear() {
    ids.value = new Set()
  }

  async function toggle(productId: number): Promise<boolean> {
    const wasFavorite = isFavorite(productId)
    if (wasFavorite) {
      ids.value.delete(productId)
    } else {
      ids.value.add(productId)
    }
    try {
      if (wasFavorite) {
        await removeFavorite(productId)
      } else {
        await addFavorite(productId)
      }
      return !wasFavorite
    } catch {
      // 回滚
      if (wasFavorite) {
        ids.value.add(productId)
      } else {
        ids.value.delete(productId)
      }
      throw new Error('操作失败')
    }
  }

  return { ids, loading, isFavorite, refresh, clear, toggle }
})
