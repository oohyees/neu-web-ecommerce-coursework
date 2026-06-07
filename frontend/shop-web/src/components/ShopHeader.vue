<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const userMenuOpen = ref(false)

const isHome = computed(() => route.path === '/')

function goSearch(kw?: string) {
  const keyword = kw || searchKeyword.value.trim()
  if (keyword) router.push({ path: '/search', query: { keyword } })
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value
}

function closeUserMenu() {
  userMenuOpen.value = false
}

function handleMenuAction(action: string) {
  closeUserMenu()
  if (action === 'profile') router.push('/profile')
  else if (action === 'settings') router.push('/profile')
  else if (action === 'security') router.push('/profile')
  else if (action === 'logout') userStore.logout()
}

function handleClickOutside(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.user-avatar-module')) closeUserMenu()
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<template>
  <header class="shop-header" :class="{ 'shop-header--home': isHome }">
    <div class="header-inner">
      <!-- 品牌 -->
      <router-link to="/" class="brand">
        <span class="brand-main">优品</span>
        <span class="brand-divider">|</span>
        <span class="brand-sub">品质生活</span>
      </router-link>

      <!-- 搜索 -->
      <div class="search-area">
        <div class="search-box">
          <input
            v-model="searchKeyword"
            placeholder="搜索你想要的商品..."
            class="search-input"
            @keydown.enter="goSearch()"
          />
          <button class="search-btn" type="button" @click="goSearch()">搜索</button>
        </div>
      </div>

      <!-- 导航 + 用户 -->
      <nav class="header-nav">
        <router-link to="/cart" class="nav-link nav-cart">
          购物车
          <span v-if="cartStore.itemCount > 0" class="cart-badge">{{ cartStore.itemCount }}</span>
        </router-link>
        <router-link to="/orders" class="nav-link">我的订单</router-link>
        <router-link to="/seckill" class="nav-link">限时秒杀</router-link>

        <!-- 已登录：头像 + 下拉 -->
        <div v-if="userStore.isLoggedIn" class="user-avatar-module" @click.stop>
          <button
            class="avatar-trigger"
            :aria-label="userStore.nickname || '用户菜单'"
            aria-haspopup="true"
            :aria-expanded="userMenuOpen"
            @click="toggleUserMenu"
          >
            <img v-if="userStore.avatarUrl" :src="userStore.avatarUrl" alt="头像" class="avatar-img" />
            <span v-else class="avatar-placeholder">{{ (userStore.nickname || '用')[0] }}</span>
            <span class="avatar-name">{{ userStore.nickname || '用户' }}</span>
          </button>
          <Transition name="dropdown">
            <div v-if="userMenuOpen" class="user-dropdown" role="menu">
              <div class="dropdown-header">
                <img v-if="userStore.avatarUrl" :src="userStore.avatarUrl" alt="" class="dropdown-avatar" />
                <span v-else class="dropdown-avatar-placeholder">{{ (userStore.nickname || '用')[0] }}</span>
                <div class="dropdown-user-info">
                  <div class="dropdown-nickname">{{ userStore.nickname || '用户' }}</div>
                  <div v-if="userStore.email" class="dropdown-email">{{ userStore.email }}</div>
                </div>
              </div>
              <div class="dropdown-divider" />
              <button class="dropdown-item" role="menuitem" @click="handleMenuAction('profile')">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                个人资料
              </button>
              <button class="dropdown-item" role="menuitem" @click="handleMenuAction('settings')">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
                账号设置
              </button>
              <button class="dropdown-item" role="menuitem" @click="handleMenuAction('security')">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                安全中心
              </button>
              <div class="dropdown-divider" />
              <button class="dropdown-item dropdown-item--danger" role="menuitem" @click="handleMenuAction('logout')">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                退出登录
              </button>
            </div>
          </Transition>
        </div>

        <!-- 未登录 -->
        <template v-else>
          <router-link to="/login" class="nav-auth-btn">登录</router-link>
          <router-link to="/register" class="nav-auth-btn nav-auth-btn--primary">注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.shop-header {
  background: linear-gradient(180deg, #fff 0%, #fffaf7 55%, #fff 100%);
  border-bottom: 1px solid rgba(255, 107, 53, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 16px 20px;
}

/* ═══ 品牌 ═══ */
.brand {
  flex-shrink: 0;
  cursor: pointer;
  text-decoration: none;
  display: flex;
  align-items: baseline;
}
.brand-main {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary, #ff6b35), var(--color-primary-light, #ff8c5a));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
}
.brand-divider {
  margin: 0 8px;
  color: #e0e0e0;
  font-weight: 200;
  -webkit-text-fill-color: #e0e0e0;
}
.brand-sub {
  font-size: 14px;
  color: #555;
  font-weight: 600;
  -webkit-text-fill-color: #555;
}

/* ═══ 搜索 ═══ */
.search-area {
  flex: 1;
  max-width: 560px;
}
.search-box {
  display: flex;
  height: 42px;
  border-radius: 22px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(255, 107, 53, 0.1);
  border: 2px solid var(--color-primary, #ff6b35);
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 20px;
  font-size: 14px;
  background: transparent;
}
.search-btn {
  border: none;
  background: linear-gradient(135deg, var(--color-primary, #ff6b35), var(--color-primary-light, #ff8c5a));
  color: #fff;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: filter 0.2s;
}
.search-btn:hover { filter: brightness(1.08); }

/* ═══ 导航 ═══ */
.header-nav {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 14px;
  flex-shrink: 0;
}
.nav-link {
  color: #555;
  white-space: nowrap;
  transition: color 0.2s;
  text-decoration: none;
}
.nav-link:hover,
.nav-link.router-link-active {
  color: var(--color-primary, #ff6b35);
}
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

/* ═══ 用户头像模块 ═══ */
.user-avatar-module { position: relative; }
.avatar-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 48px;
  min-width: 48px;
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 24px;
  transition: background 0.2s;
}
.avatar-trigger:hover { background: rgba(255,107,53,0.06); }
.avatar-trigger:focus-visible { outline: 2px solid var(--color-primary, #ff6b35); outline-offset: 2px; }
.avatar-img { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; border: 2px solid rgba(255,107,53,0.2); }
.avatar-placeholder {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary, #ff6b35), #ff8c5a);
  color: #fff; font-size: 15px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.avatar-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ═══ 下拉菜单 ═══ */
.user-dropdown {
  position: absolute; top: calc(100% + 4px); right: 0;
  width: 240px; background: #fff; border-radius: 12px;
  box-shadow: 0 12px 36px rgba(0,0,0,0.12), 0 0 0 1px rgba(0,0,0,0.04);
  padding: 8px 0; z-index: 200;
}
.dropdown-header {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 16px;
}
.dropdown-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.dropdown-avatar-placeholder {
  width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, var(--color-primary, #ff6b35), #ff8c5a);
  color: #fff; font-size: 16px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.dropdown-user-info { min-width: 0; }
.dropdown-nickname { font-size: 14px; font-weight: 600; color: #1a1a1a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.dropdown-email { font-size: 12px; color: #888; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.dropdown-divider { height: 1px; background: #f0f0f0; margin: 4px 12px; }
.dropdown-item {
  display: flex; align-items: center; gap: 10px;
  width: 100%; padding: 10px 16px; border: none; background: none;
  font-size: 14px; color: #333; cursor: pointer; text-align: left;
  transition: background 0.15s, color 0.15s;
}
.dropdown-item:hover { background: #fff8f5; color: var(--color-primary, #ff6b35); }
.dropdown-item svg { flex-shrink: 0; opacity: 0.6; }
.dropdown-item:hover svg { opacity: 1; }
.dropdown-item--danger { color: #dc2626; }
.dropdown-item--danger:hover { background: #fff1f1; color: #dc2626; }

/* 下拉过渡动画 */
.dropdown-enter-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.dropdown-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.dropdown-enter-from { opacity: 0; transform: translateY(-8px) scale(0.96); }
.dropdown-leave-to { opacity: 0; transform: translateY(-4px) scale(0.98); }

/* ═══ 未登录按钮 ═══ */
.nav-auth-btn {
  padding: 6px 16px; border-radius: 6px; font-size: 14px;
  border: 1px solid var(--color-border, #ebebeb); color: #333;
  text-decoration: none; transition: all 0.2s;
}
.nav-auth-btn:hover { border-color: var(--color-primary, #ff6b35); color: var(--color-primary, #ff6b35); }
.nav-auth-btn--primary {
  background: var(--color-primary, #ff6b35); color: #fff;
  border-color: var(--color-primary, #ff6b35);
}
.nav-auth-btn--primary:hover { background: var(--color-primary-light, #ff8c5a); border-color: var(--color-primary-light, #ff8c5a); color: #fff; }

/* ═══ 首页变体：更大更舒展 ═══ */
.shop-header--home .header-inner {
  padding: 24px 20px;
}
.shop-header--home .brand-main {
  font-size: 40px;
  letter-spacing: 3px;
}
.shop-header--home .brand-sub {
  font-size: 17px;
}
.shop-header--home .search-area {
  max-width: 640px;
}
.shop-header--home .search-box {
  height: 48px;
  border-radius: 26px;
}
.shop-header--home .search-input {
  padding: 0 22px;
  font-size: 15px;
}
.shop-header--home .search-btn {
  padding: 0 32px;
  font-size: 16px;
}

/* ═══ 响应式 ═══ */
@media (max-width: 1100px) {
  .nav-link.hide-md { display: none; }
}

@media (max-width: 768px) {
  .header-inner { gap: 12px; padding: 10px 12px !important; }
  .brand-main { font-size: 22px !important; letter-spacing: 1px !important; }
  .brand-sub { display: none; }
  .brand-divider { display: none; }
  .search-box { height: 38px !important; }
  .search-btn { padding: 0 16px !important; font-size: 14px !important; }
  .header-nav { gap: 10px; font-size: 13px; }
  .nav-link.hide-sm { display: none; }
  .avatar-name { display: none; }
  .avatar-trigger { min-width: 44px; min-height: 44px; padding: 4px; }
  .avatar-img, .avatar-placeholder { width: 32px; height: 32px; font-size: 14px; }
  .user-dropdown { width: 220px; right: -8px; }
  .nav-auth-btn { padding: 5px 12px; font-size: 13px; }
}
</style>
