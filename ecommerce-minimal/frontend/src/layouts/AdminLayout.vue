<template>
  <el-container class="admin-shell">
    <el-aside width="236px" class="aside">
      <div class="brand" @click="$router.push('/admin/dashboard')">
        <strong>EC</strong>
        <span>商城运营后台</span>
      </div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/admin/dashboard">
          <span>数据看板</span>
        </el-menu-item>
        <el-sub-menu index="goods">
          <template #title>商品中心</template>
          <el-menu-item index="/admin/products">商品管理</el-menu-item>
          <el-menu-item index="/admin/categories">分类管理</el-menu-item>
          <el-menu-item index="/admin/reviews">评价管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="orders">
          <template #title>交易中心</template>
          <el-menu-item index="/admin/orders">订单管理</el-menu-item>
          <el-menu-item index="/admin/consultations">客服咨询</el-menu-item>
          <el-menu-item index="/admin/feedback">反馈管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="content">
          <template #title>内容营销</template>
          <el-menu-item index="/admin/banners">轮播管理</el-menu-item>
          <el-menu-item index="/admin/promotions">促销管理</el-menu-item>
          <el-menu-item v-if="session.role==='SUPER_ADMIN'" index="/admin/announcements">公告管理</el-menu-item>
          <el-menu-item v-if="session.role==='SUPER_ADMIN'" index="/admin/activity-notices">活动通知</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="system">
          <template #title>系统管理</template>
          <el-menu-item v-if="session.role==='SUPER_ADMIN'" index="/admin/users">用户管理</el-menu-item>
          <el-menu-item v-if="session.role==='SUPER_ADMIN'" index="/admin/admins">管理员账号</el-menu-item>
          <el-menu-item index="/admin/profile">个人资料</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <strong>管理员控制台</strong>
          <span class="role-tag">{{ session.role || 'ADMIN' }}</span>
        </div>
        <el-button @click="logout">退出登录</el-button>
      </el-header>
      <el-main class="main"><slot /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { useRouter } from 'vue-router'
import { useSessionStore } from '../store'
import { api } from '../api'
const router = useRouter(), session = useSessionStore()
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

@media (max-width: 768px) {
  .admin-shell {
    display: block;
  }
  .aside {
    width: 100% !important;
  }
  .brand {
    height: auto;
    padding: 16px 20px;
  }
  .aside :deep(.el-menu) {
    display: flex;
    flex-wrap: wrap;
  }
  .aside :deep(.el-sub-menu) {
    flex: 1 1 50%;
    min-width: 160px;
  }
  .aside :deep(.el-menu-item) {
    min-width: 0;
  }
  .header {
    height: auto;
    padding: 12px 16px;
  }
  .main {
    padding: 16px;
  }
}
</style>
