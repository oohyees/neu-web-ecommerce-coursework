import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useUserStore } from './user'
import { fetchCart } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const itemCount = ref(0)

  async function refresh() {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      itemCount.value = 0
      return
    }
    try {
      const res = await fetchCart()
      const items = (res.data as any)?.items ?? []
      itemCount.value = items.reduce((s: number, i: any) => s + i.quantity, 0)
    } catch {
      itemCount.value = 0
    }
  }

  return { itemCount, refresh }
})
