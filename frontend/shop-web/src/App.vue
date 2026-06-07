<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useFavoriteStore } from '@/stores/favorite'
import { getProfile } from '@/api/user'
import ShopHeader from '@/components/ShopHeader.vue'
import ShopFloatingActions from '@/components/ShopFloatingActions.vue'
import AccountNav from '@/components/AccountNav.vue'

const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const favoriteStore = useFavoriteStore()

const isAuthPage = computed(() => route.meta.guest === true)
const accountPaths = new Set(['/profile', '/orders', '/address', '/favorites', '/coupons', '/feedback', '/service'])
const showAccountNav = computed(() => accountPaths.has(route.path) || route.path.startsWith('/orders/'))

async function refreshUserData() {
  if (userStore.isLoggedIn) {
    cartStore.refresh()
    favoriteStore.refresh()
    try {
      const res: any = await getProfile()
      const d = res.data ?? res
      userStore.setProfile({ nickname: d.nickname, avatarUrl: d.avatarUrl, email: d.email, phone: d.phone })
    } catch { /* silent */ }
  }
}

onMounted(refreshUserData)

watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    cartStore.refresh()
    favoriteStore.refresh()
  } else {
    favoriteStore.clear()
  }
})
</script>

<template>
  <div class="app-layout">
    <!-- 全局统一顶栏 -->
    <ShopHeader v-if="!isAuthPage" />

    <!-- 页面主体 -->
    <main class="app-main" :class="{ 'app-main--home': route.path === '/', 'app-main--full': isAuthPage }">
      <AccountNav v-if="showAccountNav" />
      <router-view />
    </main>
    <ShopFloatingActions />
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 20px 16px;
}
.app-main--home,
.app-main--full {
  max-width: none;
  padding: 0;
}

@media (max-width: 768px) {
  .app-main { padding: 12px 10px; }
  .app-main--home,
  .app-main--full { padding: 0; }
}
</style>
