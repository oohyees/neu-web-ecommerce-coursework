<template>
  <el-container class="admin-shell">
    <el-aside width="236px" class="aside">
      <div class="brand" @click="$router.push('/admin/dashboard')">
        <strong>EC</strong>
        <span>商城运营后台</span>
      </div>
      <el-menu :default-active="$route.path" :default-openeds="openedMenus" router>
        <el-menu-item v-for="item in menuGroups[0].items" :key="item.path" :index="item.path">{{ item.label }}</el-menu-item>
        <el-sub-menu v-for="group in menuGroups.slice(1)" :key="group.index" :index="group.index">
          <template #title>{{ group.label }}</template>
          <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">{{ item.label }}</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button class="mobile-menu-btn" text @click="drawerVisible = true" aria-label="打开菜单">
            <span class="menu-mark"></span>
          </el-button>
          <strong>管理员控制台</strong>
          <span class="role-tag">{{ session.role || 'ADMIN' }}</span>
        </div>
        <el-button @click="logout">退出登录</el-button>
      </el-header>
      <el-main class="main"><slot /></el-main>
    </el-container>
    <el-drawer v-model="drawerVisible" title="后台导航" direction="ltr" size="280px" class="admin-drawer">
      <el-menu :default-active="$route.path" :default-openeds="openedMenus" router @select="drawerVisible = false">
        <el-menu-item v-for="item in menuGroups[0].items" :key="item.path" :index="item.path">{{ item.label }}</el-menu-item>
        <el-sub-menu v-for="group in menuGroups.slice(1)" :key="group.index" :index="group.index">
          <template #title>{{ group.label }}</template>
          <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">{{ item.label }}</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-drawer>
  </el-container>
</template>
<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSessionStore } from '../store'
import { api } from '../api'
const route = useRoute(), router = useRouter(), session = useSessionStore()
const drawerVisible = ref(false)

const menuGroups = computed(() => [
  { index: 'dashboard', items: [{ path: '/admin/dashboard', label: '数据看板' }] },
  {
    index: 'goods',
    label: '商品中心',
    items: [
      { path: '/admin/products', label: '商品管理' },
      { path: '/admin/categories', label: '分类管理' },
      { path: '/admin/reviews', label: '评价管理' }
    ]
  },
  {
    index: 'orders',
    label: '交易中心',
    items: [
      { path: '/admin/orders', label: '订单管理' },
      { path: '/admin/consultations', label: '客服咨询' },
      { path: '/admin/feedback', label: '反馈管理' }
    ]
  },
  {
    index: 'content',
    label: '内容营销',
    items: [
      { path: '/admin/banners', label: '轮播管理' },
      { path: '/admin/promotions', label: '促销管理' },
      ...(session.role === 'SUPER_ADMIN' ? [
        { path: '/admin/announcements', label: '公告管理' },
        { path: '/admin/activity-notices', label: '活动通知' }
      ] : [])
    ]
  },
  {
    index: 'system',
    label: '系统管理',
    items: [
      ...(session.role === 'SUPER_ADMIN' ? [
        { path: '/admin/users', label: '用户管理' },
        { path: '/admin/admins', label: '管理员账号' }
      ] : []),
      { path: '/admin/profile', label: '个人资料' }
    ]
  }
])

const menuRouteMap = {
  'products': 'goods', 'categories': 'goods', 'reviews': 'goods',
  'orders': 'orders', 'consultations': 'orders', 'feedback': 'orders',
  'banners': 'content', 'promotions': 'content', 'announcements': 'content', 'activity-notices': 'content',
  'users': 'system', 'admins': 'system', 'profile': 'system'
}
const openedMenus = computed(() => {
  const key = Object.keys(menuRouteMap).find(k => route.path.includes('/admin/' + k))
  return key ? [menuRouteMap[key]] : []
})
watch(() => route.path, () => { drawerVisible.value = false })

async function logout() {
  try { await api.post('/auth/logout', { token: localStorage.getItem('token') || '' }) } catch {}
  session.logout(); router.push('/admin/login')
}
</script>
<style scoped>
.admin-shell {
  min-height: 100vh;
}

.aside {
  background: #111827;
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 0 18px;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, .08);
}

.brand strong {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  background: var(--brand);
  border-radius: 8px;
  font-size: 16px;
}

.aside :deep(.el-menu) {
  border-right: none;
  background: #111827;
}

.aside :deep(.el-menu-item),
.aside :deep(.el-sub-menu__title) {
  color: #cbd5e1;
  font-size: 14px;
}

.aside :deep(.el-menu-item.is-active) {
  color: #fff;
  background: var(--brand);
}

.aside :deep(.el-sub-menu__title:hover),
.aside :deep(.el-menu-item:hover) {
  background: #1f2937;
  color: #fff;
}

.aside :deep(.el-sub-menu .el-menu) {
  background: #0f172a;
}

.aside :deep(.el-sub-menu .el-menu-item) {
  padding-left: 56px !important;
  color: #94a3b8;
}

.aside :deep(.el-sub-menu .el-menu-item.is-active) {
  color: #fff;
  background: var(--brand);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  border-bottom: 1px solid var(--line);
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-left strong {
  font-size: 16px;
}

.role-tag {
  padding: 2px 10px;
  color: var(--brand);
  font-size: 12px;
  font-weight: 600;
  background: var(--brand-light);
  border-radius: 999px;
}

.main {
  background: var(--soft);
  padding: 24px;
}

.mobile-menu-btn {
  display: none;
  width: 36px;
  height: 36px;
  padding: 0;
}

.menu-mark,
.menu-mark::before,
.menu-mark::after {
  display: block;
  width: 18px;
  height: 2px;
  background: var(--ink);
  border-radius: 999px;
  content: "";
}

.menu-mark {
  position: relative;
}

.menu-mark::before,
.menu-mark::after {
  position: absolute;
  left: 0;
}

.menu-mark::before {
  top: -6px;
}

.menu-mark::after {
  top: 6px;
}

@media (max-width: 768px) {
  .aside {
    display: none;
  }
  .header {
    position: sticky;
    top: 0;
    z-index: 10;
    height: auto;
    padding: 12px 16px;
  }
  .mobile-menu-btn {
    display: inline-flex;
  }
  .main {
    padding: 16px;
  }
}
</style>
