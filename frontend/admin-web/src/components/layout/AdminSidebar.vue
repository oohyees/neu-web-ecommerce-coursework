<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

defineProps<{ mobile?: boolean }>()
const emit = defineEmits<{ navigate: [] }>()

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

function onClick(path: string) {
  router.push(path)
  emit('navigate')
}

const menuGroups = computed(() => {
  const groups: { index: string; label?: string; items: { path: string; label: string; permission?: string; superOnly?: boolean }[] }[] = [
    {
      index: 'dashboard',
      items: [{ path: '/dashboard', label: '数据看板', permission: 'dashboard:view' }],
    },
    {
      index: 'goods',
      label: '商品中心',
      items: [
        { path: '/products', label: '商品管理', permission: 'product:manage' },
        { path: '/categories', label: '分类管理', permission: 'category:manage' },
        { path: '/reviews', label: '评价管理', permission: 'review:manage' },
        { path: '/promotions', label: '促销管理', permission: 'promotion:manage' },
      ],
    },
    {
      index: 'orders',
      label: '交易管理',
      items: [
        { path: '/orders', label: '订单管理', permission: 'order:manage' },
      ],
    },
    {
      index: 'users',
      label: '用户管理',
      items: [
        { path: '/users', label: '用户管理', permission: 'user:manage' },
      ],
    },
    {
      index: 'content',
      label: '内容管理',
      items: [
        { path: '/banners', label: '轮播管理', permission: 'banner:manage' },
        { path: '/notices', label: '公告管理', permission: 'notice:manage' },
        { path: '/feedbacks', label: '反馈管理', permission: 'feedback:manage' },
        { path: '/cs', label: '客服咨询', permission: 'cs:manage' },
      ],
    },
    {
      index: 'system',
      label: '系统设置',
      items: [
        { path: '/permission-manage', label: '权限管理', superOnly: true },
        { path: '/profile', label: '个人中心' },
      ],
    },
  ]
  return groups
})

function isVisible(item: { permission?: string; superOnly?: boolean }): boolean {
  if (item.superOnly && adminStore.role !== 'SUPER_ADMIN') return false
  if (item.permission && !adminStore.hasPermission(item.permission)) return false
  return true
}
</script>

<template>
  <aside class="admin-sidebar">
    <div class="sidebar-brand" @click="onClick('/dashboard')">
      <strong>优品</strong>
      <span>商城运营后台</span>
    </div>
    <el-menu
      :default-active="route.path"
      :default-openeds="['goods', 'orders', 'users', 'content', 'system']"
      class="sidebar-menu"
    >
      <template v-for="group in menuGroups" :key="group.index">
        <template v-if="!group.label">
          <el-menu-item
            v-for="item in group.items"
            :key="item.path"
            :index="item.path"
            @click="onClick(item.path)"
          >
            {{ item.label }}
          </el-menu-item>
        </template>
        <el-sub-menu v-else :index="group.index">
          <template #title>{{ group.label }}</template>
          <el-menu-item
            v-for="item in group.items.filter(isVisible)"
            :key="item.path"
            :index="item.path"
            @click="onClick(item.path)"
          >
            {{ item.label }}
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
    <div class="sidebar-footer">
      <span class="role-tag">{{ adminStore.role || 'ADMIN' }}</span>
      <span class="logout-btn" @click="adminStore.logout(); onClick('/login')">退出</span>
    </div>
  </aside>
</template>

<style scoped>
.admin-sidebar {
  width: 236px;
  min-height: 100vh;
  background: #1f2d3d;
  color: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.sidebar-brand {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.sidebar-brand strong {
  font-size: 18px;
  color: var(--color-primary, #ff6b35);
}
.sidebar-brand span {
  font-size: 13px;
  color: rgba(255,255,255,0.65);
}
.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
}
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: rgba(255,255,255,0.75);
}
.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255,255,255,0.06);
  color: #fff;
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--color-primary, #ff6b35);
  color: #fff;
}
.sidebar-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255,255,255,0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.role-tag {
  background: var(--color-primary, #ff6b35);
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
}
.logout-btn {
  color: rgba(255,255,255,0.55);
  cursor: pointer;
}
.logout-btn:hover { color: #fff; }
</style>
