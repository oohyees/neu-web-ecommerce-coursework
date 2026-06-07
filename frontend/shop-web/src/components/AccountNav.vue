<script setup lang="ts">
import {
  ChatDotRound,
  Collection,
  Discount,
  House,
  Location,
  Message,
  Tickets,
  User,
} from '@element-plus/icons-vue'

const items = [
  { to: '/profile', label: '个人资料', icon: User },
  { to: '/orders', label: '我的订单', icon: Tickets },
  { to: '/address', label: '收货地址', icon: Location },
  { to: '/favorites', label: '我的收藏', icon: Collection },
  { to: '/coupons', label: '优惠券', icon: Discount },
  { to: '/feedback', label: '意见反馈', icon: Message },
  { to: '/service', label: '在线客服', icon: ChatDotRound },
  { to: '/products', label: '继续购物', icon: House },
]
</script>

<template>
  <nav class="account-nav" aria-label="我的账户导航">
    <router-link v-for="item in items" :key="item.to" :to="item.to" class="account-nav-item">
      <el-icon><component :is="item.icon" /></el-icon>
      <span>{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<style scoped>
.account-nav {
  max-width: var(--max-width);
  margin: 0 auto 16px;
  padding: 12px;
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 8px;
  background: #fff;
  border: 1px solid rgba(17, 24, 39, 0.06);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(17, 24, 39, 0.06);
}
.account-nav-item {
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 10px;
  border-radius: 8px;
  color: #4b5563;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}
.account-nav-item:hover,
.account-nav-item.router-link-active {
  color: var(--color-primary);
  background: var(--color-primary-lighter);
}
@media (max-width: 1024px) {
  .account-nav { grid-template-columns: repeat(4, minmax(0, 1fr)); }
}
@media (max-width: 640px) {
  .account-nav { grid-template-columns: repeat(2, minmax(0, 1fr)); margin-bottom: 12px; }
}
</style>
