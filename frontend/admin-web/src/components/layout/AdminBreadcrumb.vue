<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const titleMap: Record<string, string> = {
  '/dashboard': '数据看板',
  '/users': '用户管理',
  '/categories': '分类管理',
  '/products': '商品管理',
  '/promotions': '促销管理',
  '/reviews': '评价管理',
  '/orders': '订单管理',
  '/banners': '轮播管理',
  '/notices': '公告管理',
  '/feedbacks': '反馈管理',
  '/cs': '客服咨询',
  '/permission-manage': '权限管理',
  '/roles': '权限管理',
  '/admins': '权限管理',
  '/profile': '个人中心',
}

const currentTitle = computed(() => titleMap[route.path] || String(route.meta.title || '管理后台'))
</script>

<template>
  <nav class="admin-breadcrumb" aria-label="后台页面位置">
    <router-link to="/dashboard">管理后台</router-link>
    <span>/</span>
    <span>{{ currentTitle }}</span>
  </nav>
</template>

<style scoped>
.admin-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(17, 24, 39, 0.06);
  border-radius: 8px;
  color: #8a94a6;
  font-size: 13px;
  box-shadow: 0 10px 30px rgba(17, 24, 39, 0.05);
}
.admin-breadcrumb a { color: #5b6472; font-weight: 700; }
.admin-breadcrumb a:hover { color: var(--color-primary); }
.admin-breadcrumb span:last-child { color: #1f2d3d; font-weight: 800; }
@media (max-width: 900px) {
  .admin-breadcrumb { margin-bottom: 12px; }
}
</style>
