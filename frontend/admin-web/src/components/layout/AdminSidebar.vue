<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import {
  Bell,
  ChatDotSquare,
  DataBoard,
  Goods,
  Key,
  List,
  Menu,
  Message,
  Picture,
  Promotion,
  Setting,
  Shop,
  User,
} from '@element-plus/icons-vue'

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
  const groups: { index: string; label?: string; items: { path: string; label: string; permission?: string; superOnly?: boolean; icon?: unknown }[] }[] = [
    {
      index: 'dashboard',
      items: [{ path: '/dashboard', label: '数据看板', permission: 'dashboard:view', icon: DataBoard }],
    },
    {
      index: 'goods',
      label: '商品中心',
      items: [
        { path: '/products', label: '商品管理', permission: 'product:manage', icon: Goods },
        { path: '/categories', label: '分类管理', permission: 'category:manage', icon: Menu },
        { path: '/reviews', label: '评价管理', permission: 'review:manage', icon: ChatDotSquare },
        { path: '/promotions', label: '促销管理', permission: 'promotion:manage', icon: Promotion },
      ],
    },
    {
      index: 'orders',
      label: '交易管理',
      items: [
        { path: '/orders', label: '订单管理', permission: 'order:manage', icon: List },
      ],
    },
    {
      index: 'users',
      label: '用户管理',
      items: [
        { path: '/users', label: '用户管理', permission: 'user:manage', icon: User },
      ],
    },
    {
      index: 'content',
      label: '内容管理',
      items: [
        { path: '/banners', label: '轮播管理', permission: 'banner:manage', icon: Picture },
        { path: '/notices', label: '公告管理', permission: 'notice:manage', icon: Bell },
        { path: '/feedbacks', label: '反馈管理', permission: 'feedback:manage', icon: Message },
        { path: '/cs', label: '客服咨询', permission: 'cs:manage', icon: ChatDotSquare },
      ],
    },
    {
      index: 'system',
      label: '系统设置',
      items: [
        { path: '/permission-manage', label: '权限管理', superOnly: true, icon: Key },
        { path: '/profile', label: '个人中心', icon: Setting },
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
      <span class="brand-mark"><el-icon><Shop /></el-icon></span>
      <span class="brand-copy">
        <strong>优品</strong>
        <span>商城运营后台</span>
      </span>
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
            <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
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
            <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
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
  background:
    linear-gradient(180deg, rgba(255, 107, 53, 0.08), transparent 22%),
    var(--color-sidebar, #182433);
  color: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: 10px 0 30px rgba(18, 28, 40, 0.14);
}
.sidebar-brand {
  padding: 22px 22px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: var(--color-primary, #ff6b35);
  color: #fff;
  box-shadow: 0 10px 20px rgba(255, 107, 53, 0.22);
}
.brand-copy {
  display: grid;
  gap: 2px;
}
.sidebar-brand strong {
  font-size: 18px;
  color: #fff;
  letter-spacing: 0;
}
.sidebar-brand span {
  font-size: 13px;
  color: rgba(255,255,255,0.65);
}
.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 12px 12px 16px;
}
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: rgba(255,255,255,0.75);
  height: 44px;
  margin: 4px 0;
  border-radius: 8px;
}
.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255,255,255,0.06);
  color: #fff;
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--color-primary, #ff6b35);
  color: #fff;
  box-shadow: 0 8px 18px rgba(255, 107, 53, 0.24);
}
.sidebar-menu :deep(.el-sub-menu__title) {
  font-weight: 700;
}
.sidebar-menu :deep(.el-menu-item .el-icon),
.sidebar-menu :deep(.el-sub-menu__title .el-icon) {
  margin-right: 8px;
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
