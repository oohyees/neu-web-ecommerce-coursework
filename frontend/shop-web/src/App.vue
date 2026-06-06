<script setup lang="ts">
import { computed, onMounted, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useFavoriteStore } from '@/stores/favorite'
import ShopFloatingActions from '@/components/ShopFloatingActions.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const favoriteStore = useFavoriteStore()
const searchKeyword = ref('')

const isHome = computed(() => route.path === '/')
const isAuthPage = computed(() => route.meta.guest === true)
const showHeader = computed(() => !isHome.value && !isAuthPage.value)

function goSearch() {
  const kw = searchKeyword.value.trim()
  if (kw) router.push({ path: '/search', query: { keyword: kw } })
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    cartStore.refresh()
    favoriteStore.refresh()
  }
})

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
    <!-- 活动通知横幅占位 -->
    <div v-if="!isAuthPage" class="activity-banner">
      <slot name="banner" />
    </div>

    <!-- 全局顶栏 -->
    <header v-if="showHeader" class="app-header">
      <div class="header-inner">
        <router-link to="/" class="header-logo">优品商城</router-link>
        <div class="header-search">
          <input
            v-model="searchKeyword"
            placeholder="搜索商品"
            class="header-search-input"
            @keydown.enter="goSearch"
          />
        </div>
        <nav class="header-nav">
          <router-link to="/cart" class="nav-cart">
            购物车
            <span v-if="cartStore.itemCount > 0" class="cart-badge">{{ cartStore.itemCount }}</span>
          </router-link>
          <template v-if="userStore.isLoggedIn">
            <router-link to="/profile">{{ userStore.nickname || '个人中心' }}</router-link>
            <a href="#" @click.prevent="userStore.logout()">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <router-link to="/register">注册</router-link>
          </template>
        </nav>
      </div>
    </header>

    <!-- 页面主体 -->
    <main class="app-main" :class="{ 'app-main--home': isHome, 'app-main--full': isAuthPage }">
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

.app-header {
  background: #fff;
  border-bottom: 1px solid var(--color-border, #ebebeb);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 20px;
  height: 56px;
}

.header-logo {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary, #ff6b35);
  flex-shrink: 0;
}

.header-search {
  flex: 1;
  max-width: 480px;
}

.header-search-input {
  width: 100%;
  height: 36px;
  padding: 0 16px;
  border: 1px solid var(--color-border, #ebebeb);
  border-radius: 18px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.header-search-input:focus {
  border-color: var(--color-primary, #ff6b35);
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  flex-shrink: 0;
}
.header-nav a { color: #333; transition: color 0.2s; }
.header-nav a:hover { color: var(--color-primary, #ff6b35); }

.nav-cart { position: relative; }
.cart-badge {
  position: absolute;
  top: -8px;
  right: -12px;
  background: var(--color-primary, #ff6b35);
  color: #fff;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
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
  .header-inner { gap: 12px; padding: 0 10px; }
  .header-nav { gap: 10px; font-size: 13px; }
  .app-main { padding: 12px 10px; }
  .app-main--home,
  .app-main--full { padding: 0; }
}
</style>
