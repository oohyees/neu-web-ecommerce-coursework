<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const trail = computed(() => {
  const path = route.path
  if (path === '/' || route.meta.guest) return []
  if (path === '/products') return [{ label: '首页', to: '/' }, { label: '商品列表' }]
  if (path === '/search') return [{ label: '首页', to: '/' }, { label: '商品搜索' }]
  if (path.startsWith('/category/')) return [{ label: '首页', to: '/' }, { label: '分类商品' }]
  if (path.startsWith('/product/')) return [{ label: '首页', to: '/' }, { label: '商品列表', to: '/products' }, { label: '商品详情' }]
  if (path === '/cart') return [{ label: '首页', to: '/' }, { label: '购物车' }]
  if (path === '/checkout') return [{ label: '购物车', to: '/cart' }, { label: '确认订单' }]
  if (path === '/payment') return [{ label: '确认订单', to: '/checkout' }, { label: '订单支付' }]
  if (path === '/orders') return [{ label: '首页', to: '/' }, { label: '我的订单' }]
  if (path.startsWith('/orders/')) return [{ label: '我的订单', to: '/orders' }, { label: '订单详情' }]
  if (path === '/seckill') return [{ label: '首页', to: '/' }, { label: '限时秒杀' }]
  if (path === '/notices') return [{ label: '首页', to: '/' }, { label: '公告活动' }]
  const accountTitles: Record<string, string> = {
    '/profile': '个人资料',
    '/address': '收货地址',
    '/favorites': '我的收藏',
    '/coupons': '优惠券',
    '/feedback': '意见反馈',
    '/service': '在线客服',
  }
  if (accountTitles[path]) return [{ label: '首页', to: '/' }, { label: '我的账户', to: '/profile' }, { label: accountTitles[path] }]
  return []
})
</script>

<template>
  <nav v-if="trail.length" class="shop-breadcrumb" aria-label="页面位置">
    <template v-for="(item, index) in trail" :key="item.label">
      <router-link v-if="item.to && index < trail.length - 1" :to="item.to">{{ item.label }}</router-link>
      <span v-else>{{ item.label }}</span>
      <span v-if="index < trail.length - 1" class="sep">/</span>
    </template>
  </nav>
</template>

<style scoped>
.shop-breadcrumb {
  max-width: var(--max-width);
  margin: 0 auto 12px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8a94a6;
  font-size: 13px;
}
.shop-breadcrumb a { color: #6b7280; }
.shop-breadcrumb a:hover { color: var(--color-primary); }
.sep { color: #c7cdd6; }
@media (max-width: 768px) {
  .shop-breadcrumb { padding: 0 10px; margin-bottom: 8px; overflow-x: auto; white-space: nowrap; }
}
</style>
