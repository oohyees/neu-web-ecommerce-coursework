<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { fetchCategories, type Category } from '@/api/product'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const userMenuOpen = ref(false)
const categories = ref<Category[]>([])
const hoverCatId = ref<number | null>(null)
const activeNavPopup = ref<string | null>(null)

const topLevelCategories = computed(() =>
  categories.value.filter(c => !c.parentId)
)

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
  closeNavPopup()
  if (action === 'profile') router.push('/profile')
  else if (action === 'orders') router.push('/orders')
  else if (action === 'address') router.push('/address')
  else if (action === 'favorites') router.push('/favorites')
  else if (action === 'coupons') router.push('/coupons')
  else if (action === 'feedback') router.push('/feedback')
  else if (action === 'service') router.push('/service')
  else if (action === 'logout') userStore.logout()
}

function toggleNavPopup(name: string) {
  activeNavPopup.value = activeNavPopup.value === name ? null : name
  // 打开分类面板时默认选中第一个分类
  if (activeNavPopup.value === 'category' && topLevelCategories.value.length && !hoverCatId.value) {
    hoverCatId.value = topLevelCategories.value[0].id
  }
}

function closeNavPopup() {
  activeNavPopup.value = null
  hoverCatId.value = null
}

function goCategory(id: number) {
  closeNavPopup()
  router.push(`/category/${id}`)
}

function goOrderStatus(status: string) {
  closeNavPopup()
  router.push({ path: '/orders', query: status ? { status } : {} })
}

function goAccountPage(path: string) {
  closeNavPopup()
  router.push(path)
}

async function loadCategories() {
  try {
    const res: any = await fetchCategories()
    const flat: Category[] = res.data ?? res
    // 将扁平列表组装成树形结构
    const map = new Map<number, Category>()
    flat.forEach(c => map.set(c.id, { ...c, children: [] }))
    const tree: Category[] = []
    flat.forEach(c => {
      const node = map.get(c.id)!
      if (c.parentId && map.has(c.parentId)) {
        map.get(c.parentId)!.children!.push(node)
      } else {
        tree.push(node)
      }
    })
    categories.value = tree
  } catch { /* 静默 */ }
}

function handleClickOutside(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.user-avatar-module')) closeUserMenu()
  if (!target.closest('.nav-popup-trigger') && !target.closest('.nav-popup-panel')) {
    activeNavPopup.value = null
    hoverCatId.value = null
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    closeNavPopup()
    closeUserMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleKeydown)
  loadCategories()
})
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <header class="shop-header">
    <!-- 第一行：品牌 + 搜索 + 用户 -->
    <div class="header-top">
      <div class="header-top-inner">
        <router-link to="/" class="brand">
          <span class="brand-main">优品</span>
          <span class="brand-divider">|</span>
          <span class="brand-sub">品质生活</span>
        </router-link>

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

        <div class="header-user">
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
                <button class="dropdown-item" role="menuitem" @click="handleMenuAction('orders')">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 6h13"/><path d="M8 12h13"/><path d="M8 18h13"/><path d="M3 6h.01"/><path d="M3 12h.01"/><path d="M3 18h.01"/></svg>
                  我的订单
                </button>
                <button class="dropdown-item" role="menuitem" @click="handleMenuAction('address')">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 1 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>
                  收货地址
                </button>
                <button class="dropdown-item" role="menuitem" @click="handleMenuAction('favorites')">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="m12 21-1.45-1.32C5.4 15 2 11.9 2 8.1 1 2.6 4.4 2.6 7.5 2.6c1.75 0 3.45.82 4.5 2.1a6 6 0 0 1 4.5-2.1C19.6 2.6 22 5 22 8.1c0 3.8-3.4 6.9-8.55 11.58L12 21Z"/></svg>
                  我的收藏
                </button>
                <button class="dropdown-item" role="menuitem" @click="handleMenuAction('coupons')">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 8a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v3a2 2 0 0 0 0 4v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a2 2 0 0 0 0-4Z"/><path d="M13 5v14"/></svg>
                  优惠券
                </button>
                <button class="dropdown-item" role="menuitem" @click="handleMenuAction('feedback')">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a4 4 0 0 1-4 4H7l-4 4V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4Z"/></svg>
                  意见反馈
                </button>
                <button class="dropdown-item" role="menuitem" @click="handleMenuAction('service')">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3a8 8 0 0 0-8 8v3a3 3 0 0 0 3 3h1v-6H6a6 6 0 0 1 12 0h-2v6h1a3 3 0 0 0 3-3v-3a8 8 0 0 0-8-8Z"/><path d="M16 17c0 2-2 3-4 3"/></svg>
                  在线客服
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
        </div>
      </div>
    </div>

    <!-- 第二行：导航条 -->
    <nav class="header-nav-bar">
      <div class="nav-bar-inner">
        <!-- 首页 -->
        <router-link to="/" class="nav-bar-item" :class="{ active: route.path === '/' }">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          首页
        </router-link>

        <!-- 商品分类 -->
        <div class="nav-popup-trigger" @mouseenter="activeNavPopup = 'category'; if(!hoverCatId && topLevelCategories.length) hoverCatId = topLevelCategories[0].id" @mouseleave="activeNavPopup = null; hoverCatId = null">
          <button
            class="nav-bar-item"
            :class="{ active: activeNavPopup === 'category' || route.path.startsWith('/category') || route.path.startsWith('/product') || route.path === '/search' || route.path === '/products' }"
            @click="toggleNavPopup('category')"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
            商品分类
            <svg class="arrow-icon" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </button>
          <Transition name="popup">
            <div v-if="activeNavPopup === 'category' && topLevelCategories.length" class="nav-popup-panel category-panel">
              <div class="category-layout">
                <ul class="cat-l1-list">
                  <li
                    v-for="cat in topLevelCategories"
                    :key="cat.id"
                    class="cat-l1-item"
                    :class="{ hover: hoverCatId === cat.id }"
                    @mouseenter="hoverCatId = cat.id"
                    @click="goCategory(cat.id)"
                  >
                    <span class="cat-name">{{ cat.name }}</span>
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
                  </li>
                </ul>
                <div v-if="hoverCatId" class="cat-l2-area">
                  <template v-for="cat in topLevelCategories" :key="cat.id">
                    <div v-if="hoverCatId === cat.id && cat.children?.length" class="cat-l2-grid">
                      <a v-for="sub in cat.children" :key="sub.id" class="cat-l2-item" @click.stop="goCategory(sub.id)">
                        {{ sub.name }}
                      </a>
                    </div>
                    <div v-if="hoverCatId === cat.id && !cat.children?.length" class="cat-l2-empty">
                      <span>点击查看「{{ cat.name }}」全部商品</span>
                    </div>
                  </template>
                </div>
              </div>
              <div class="category-shortcuts">
                <a class="shortcut-link" @click="closeNavPopup(); router.push('/search')">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                  商品搜索
                </a>
                <a class="shortcut-link" @click="closeNavPopup(); router.push('/favorites')">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="m12 21-1.45-1.32C5.4 15 2 11.9 2 8.1 2 5 4.4 2.6 7.5 2.6c1.75 0 3.45.82 4.5 2.1a6 6 0 0 1 4.5-2.1C19.6 2.6 22 5 22 8.1c0 3.8-3.4 6.9-8.55 11.58L12 21Z"/></svg>
                  我的收藏
                </a>
                <a class="shortcut-link" @click="closeNavPopup(); router.push('/products')">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
                  全部商品
                </a>
              </div>
            </div>
          </Transition>
        </div>

        <!-- 购物车 -->
        <router-link to="/cart" class="nav-bar-item nav-bar-cart" :class="{ active: route.path === '/cart' }">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
          购物车
          <span v-if="cartStore.itemCount > 0" class="nav-cart-badge">{{ cartStore.itemCount }}</span>
        </router-link>

        <!-- 我的订单 -->
        <div class="nav-popup-trigger" @mouseenter="activeNavPopup = 'orders'" @mouseleave="activeNavPopup = null">
          <button
            class="nav-bar-item"
            :class="{ active: activeNavPopup === 'orders' || route.path.startsWith('/orders') || route.path === '/checkout' || route.path === '/payment' }"
            @click="toggleNavPopup('orders')"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
            我的订单
            <svg class="arrow-icon" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </button>
          <Transition name="popup">
            <div v-if="activeNavPopup === 'orders'" class="nav-popup-panel orders-panel">
              <div class="panel-title">订单状态筛选</div>
              <div class="orders-status-grid">
                <button class="order-status-btn" @click="goOrderStatus('')">
                  <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
                  <span class="status-label">全部订单</span>
                  <span class="status-desc">查看所有订单</span>
                </button>
                <button class="order-status-btn" @click="goOrderStatus('PENDING')">
                  <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/></svg>
                  <span class="status-label">待支付</span>
                  <span class="status-desc">等待付款确认</span>
                </button>
                <button class="order-status-btn" @click="goOrderStatus('PAID')">
                  <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
                  <span class="status-label">待发货</span>
                  <span class="status-desc">已付款等待发货</span>
                </button>
                <button class="order-status-btn" @click="goOrderStatus('SHIPPED')">
                  <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
                  <span class="status-label">待收货</span>
                  <span class="status-desc">已发货运输中</span>
                </button>
                <button class="order-status-btn" @click="goOrderStatus('COMPLETED')">
                  <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                  <span class="status-label">已完成</span>
                  <span class="status-desc">交易完成可评价</span>
                </button>
                <button class="order-status-btn" @click="goOrderStatus('CANCELLED')">
                  <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                  <span class="status-label">已取消</span>
                  <span class="status-desc">订单已取消</span>
                </button>
              </div>
            </div>
          </Transition>
        </div>

        <!-- 我的账户 -->
        <div class="nav-popup-trigger" @mouseenter="activeNavPopup = 'account'" @mouseleave="activeNavPopup = null">
          <button
            class="nav-bar-item"
            :class="{ active: activeNavPopup === 'account' || route.path === '/profile' || route.path === '/address' || route.path === '/favorites' || route.path === '/coupons' || route.path === '/feedback' || route.path === '/service' }"
            @click="toggleNavPopup('account')"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            我的账户
            <svg class="arrow-icon" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </button>
          <Transition name="popup">
            <div v-if="activeNavPopup === 'account'" class="nav-popup-panel account-panel">
              <div class="panel-title">我的账户</div>
              <div class="account-section">
                <div class="account-section-title">个人设置</div>
                <div class="account-row">
                  <button class="account-item" @click="goAccountPage('/profile')">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    <div class="account-item-text">
                      <span class="account-item-label">个人中心</span>
                      <span class="account-item-desc">修改资料与密码</span>
                    </div>
                  </button>
                  <button class="account-item" @click="goAccountPage('/address')">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 1 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>
                    <div class="account-item-text">
                      <span class="account-item-label">收货地址</span>
                      <span class="account-item-desc">管理配送地址</span>
                    </div>
                  </button>
                </div>
              </div>
              <div class="account-section">
                <div class="account-section-title">资产与优惠</div>
                <div class="account-row">
                  <button class="account-item" @click="goAccountPage('/favorites')">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="m12 21-1.45-1.32C5.4 15 2 11.9 2 8.1 2 5 4.4 2.6 7.5 2.6c1.75 0 3.45.82 4.5 2.1a6 6 0 0 1 4.5-2.1C19.6 2.6 22 5 22 8.1c0 3.8-3.4 6.9-8.55 11.58L12 21Z"/></svg>
                    <div class="account-item-text">
                      <span class="account-item-label">我的收藏</span>
                      <span class="account-item-desc">收藏的商品</span>
                    </div>
                  </button>
                  <button class="account-item" @click="goAccountPage('/coupons')">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 8a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v3a2 2 0 0 0 0 4v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a2 2 0 0 0 0-4Z"/><path d="M13 5v14"/></svg>
                    <div class="account-item-text">
                      <span class="account-item-label">优惠券</span>
                      <span class="account-item-desc">可用优惠券</span>
                    </div>
                  </button>
                </div>
              </div>
              <div class="account-section">
                <div class="account-section-title">帮助与服务</div>
                <div class="account-row">
                  <button class="account-item" @click="goAccountPage('/feedback')">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a4 4 0 0 1-4 4H7l-4 4V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4Z"/></svg>
                    <div class="account-item-text">
                      <span class="account-item-label">意见反馈</span>
                      <span class="account-item-desc">提交建议与问题</span>
                    </div>
                  </button>
                  <button class="account-item" @click="goAccountPage('/service')">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 3a8 8 0 0 0-8 8v3a3 3 0 0 0 3 3h1v-6H6a6 6 0 0 1 12 0h-2v6h1a3 3 0 0 0 3-3v-3a8 8 0 0 0-8-8Z"/><path d="M16 17c0 2-2 3-4 3"/></svg>
                    <div class="account-item-text">
                      <span class="account-item-label">在线客服</span>
                      <span class="account-item-desc">联系客服咨询</span>
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </Transition>
        </div>

        <!-- 秒杀·优惠 -->
        <router-link to="/seckill" class="nav-bar-item nav-bar-seckill" :class="{ active: route.path === '/seckill' }">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
          秒杀·优惠
        </router-link>

        <!-- 公告活动 -->
        <router-link to="/notices" class="nav-bar-item" :class="{ active: route.path === '/notices' }">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
          公告活动
        </router-link>
      </div>
    </nav>
  </header>
</template>

<style scoped>
.shop-header {
  background: #fff;
  border-bottom: 1px solid rgba(255, 107, 53, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

/* ═══ 第一行：品牌 + 搜索 + 用户 ═══ */
.header-top {
  background: linear-gradient(180deg, #fff 0%, #fffaf7 55%, #fff 100%);
}

.header-top-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
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

/* ═══ 用户区域 ═══ */
.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

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

/* ═══ 用户下拉菜单 ═══ */
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

/* ═══ 第二行：导航条 ═══ */
.header-nav-bar {
  background: linear-gradient(90deg, var(--color-primary, #ff6b35), var(--color-primary-light, #ff8c5a));
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.15);
}

.nav-bar-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 0 20px;
  height: 42px;
}

.nav-bar-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 18px;
  height: 42px;
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  border: none;
  background: none;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s, color 0.2s;
  position: relative;
}

.nav-bar-item:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.nav-bar-item.active {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: 600;
}

.nav-bar-item svg {
  flex-shrink: 0;
}

.arrow-icon {
  transition: transform 0.2s;
}
.nav-bar-item.active .arrow-icon {
  transform: rotate(180deg);
}

/* 购物车 badge */
.nav-bar-cart {
  position: relative;
}
.nav-cart-badge {
  position: absolute;
  top: 4px;
  right: 6px;
  background: #ff0036;
  color: #fff;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

/* 秒杀特殊样式 */
.nav-bar-seckill {
  animation: seckill-pulse 2s ease-in-out infinite;
}
@keyframes seckill-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.85; }
}

/* ═══ 弹出面板通用 ═══ */
.nav-popup-trigger {
  position: relative;
}

.nav-popup-panel {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 12px 36px rgba(0,0,0,0.12), 0 0 0 1px rgba(0,0,0,0.04);
  z-index: 200;
  color: #333;
}

/* ═══ 分类弹出面板 ═══ */
.category-panel {
  min-width: 480px;
  padding: 0;
}

.category-layout {
  display: flex;
  min-height: 240px;
}

.cat-l1-list {
  list-style: none;
  padding: 8px 0;
  margin: 0;
  min-width: 140px;
  background: #fafafa;
  border-radius: 0 0 0 12px;
}

.cat-l1-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.15s;
}

.cat-l1-item:hover,
.cat-l1-item.hover {
  background: var(--color-primary, #ff6b35);
  color: #fff;
}

.cat-l1-item .cat-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cat-l2-area {
  flex: 1;
  padding: 16px 20px;
  min-width: 300px;
}

.cat-l2-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.cat-l2-item {
  font-size: 13px;
  color: #666;
  cursor: pointer;
  padding: 4px 0;
  transition: color 0.2s;
  text-decoration: none;
}

.cat-l2-item:hover {
  color: var(--color-primary, #ff6b35);
}

.cat-l2-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 120px;
  color: #bbb;
  font-size: 13px;
}

.category-shortcuts {
  display: flex;
  gap: 0;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
  border-radius: 0 0 12px 12px;
  overflow: hidden;
}

.shortcut-link {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 12px;
  font-size: 12px;
  color: var(--color-primary, #ff6b35);
  cursor: pointer;
  transition: background 0.15s;
  text-decoration: none;
  border-right: 1px solid #f0f0f0;
}

.shortcut-link:last-child {
  border-right: none;
}

.shortcut-link:hover {
  background: #fff5f0;
}

/* ═══ 弹窗通用标题 ═══ */
.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

/* ═══ 订单弹出面板 ═══ */
.orders-panel {
  min-width: 380px;
  padding: 16px;
}

.orders-status-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.order-status-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 8px 10px;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}

.order-status-btn:hover {
  border-color: var(--color-primary, #ff6b35);
  background: #fff8f5;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.1);
}

.order-status-btn svg {
  color: #999;
  transition: color 0.2s;
}

.order-status-btn:hover svg {
  color: var(--color-primary, #ff6b35);
}

.status-label {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.status-desc {
  font-size: 11px;
  color: #aaa;
}

.order-status-btn:hover .status-label {
  color: var(--color-primary, #ff6b35);
}

/* ═══ 账户弹出面板 ═══ */
.account-panel {
  min-width: 340px;
  padding: 16px;
}

.account-section {
  margin-bottom: 12px;
}

.account-section:last-child {
  margin-bottom: 0;
}

.account-section-title {
  font-size: 11px;
  color: #aaa;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.account-row {
  display: flex;
  gap: 8px;
}

.account-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.account-item:hover {
  border-color: var(--color-primary, #ff6b35);
  background: #fff8f5;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.1);
}

.account-item svg {
  flex-shrink: 0;
  color: #999;
  transition: color 0.2s;
}

.account-item:hover svg {
  color: var(--color-primary, #ff6b35);
}

.account-item-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.account-item-label {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.account-item:hover .account-item-label {
  color: var(--color-primary, #ff6b35);
}

.account-item-desc {
  font-size: 11px;
  color: #aaa;
}

/* ═══ 弹出过渡动画 ═══ */
.popup-enter-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.popup-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.popup-enter-from { opacity: 0; transform: translateX(-50%) translateY(-8px); }
.popup-leave-to { opacity: 0; transform: translateX(-50%) translateY(-4px); }

.dropdown-enter-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.dropdown-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.dropdown-enter-from { opacity: 0; transform: translateY(-8px) scale(0.96); }
.dropdown-leave-to { opacity: 0; transform: translateY(-4px) scale(0.98); }

/* ═══ 响应式 ═══ */
@media (max-width: 1100px) {
  .nav-bar-item {
    padding: 0 12px;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .header-top-inner {
    flex-wrap: wrap;
    gap: 8px 10px;
    padding: 10px 12px !important;
  }
  .brand-main { font-size: 22px !important; letter-spacing: 1px !important; }
  .brand-sub { display: none; }
  .brand-divider { display: none; }
  .search-area { order: 3; flex: 0 0 100%; max-width: none; }
  .search-box { height: 38px !important; }
  .search-input { min-width: 0; padding: 0 14px; }
  .search-btn { min-width: 58px; padding: 0 12px !important; font-size: 13px !important; white-space: nowrap; }
  .avatar-name { display: none; }
  .avatar-trigger { min-width: 44px; min-height: 44px; padding: 4px; }
  .avatar-img, .avatar-placeholder { width: 32px; height: 32px; font-size: 14px; }
  .user-dropdown { width: 220px; right: -8px; }
  .nav-auth-btn { padding: 5px 12px; font-size: 13px; }

  /* 导航条移动端：居中 + 横向滚动 */
  .nav-bar-inner {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
    padding: 0 12px;
    gap: 0;
    justify-content: flex-start;
  }
  .nav-bar-inner::-webkit-scrollbar { display: none; }
  .nav-bar-item {
    padding: 0 12px;
    font-size: 13px;
    flex-shrink: 0;
  }
  .nav-bar-item svg:first-child { display: none; }

  /* 弹出面板移动端适配 */
  .category-panel {
    min-width: 300px;
    left: 0;
    transform: none;
  }
  .orders-panel {
    min-width: 260px;
    left: 0;
    transform: none;
  }
  .account-panel {
    min-width: 260px;
    left: 0;
    transform: none;
  }
  .popup-enter-from { transform: translateY(-8px); }
  .popup-leave-to { transform: translateY(-4px); }
}
</style>
