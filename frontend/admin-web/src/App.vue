<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Menu } from '@element-plus/icons-vue'
import AdminSidebar from '@/components/layout/AdminSidebar.vue'

const route = useRoute()
const isLoginPage = computed(() => route.path === '/login')
const drawerVisible = ref(false)
</script>

<template>
  <div class="admin-layout" :class="{ 'admin-layout--login': isLoginPage }">
    <template v-if="!isLoginPage">
      <!-- 移动端菜单按钮 -->
      <button class="mobile-menu-btn" aria-label="打开菜单" @click="drawerVisible = true">
        <el-icon><Menu /></el-icon>
      </button>
      <!-- 桌面端侧边栏 -->
      <AdminSidebar class="desktop-sidebar" />
      <!-- 移动端抽屉 -->
      <el-drawer v-model="drawerVisible" direction="ltr" size="260px" :with-header="false" class="mobile-drawer">
        <AdminSidebar mobile @navigate="drawerVisible = false" />
      </el-drawer>
      <main class="admin-main">
        <router-view />
      </main>
    </template>
    <router-view v-else />
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}
.admin-layout--login {
  display: block;
}
.admin-main {
  flex: 1;
  padding: 24px;
  background: #f5f7fa;
  overflow: auto;
  min-width: 0;
}
.mobile-menu-btn {
  display: none;
  position: fixed;
  top: 12px;
  left: 12px;
  z-index: 120;
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 10px;
  background: #1f2d3d;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
@media (max-width: 900px) {
  .admin-layout { flex-direction: column; }
  .desktop-sidebar { display: none; }
  .mobile-menu-btn { display: inline-flex; align-items: center; justify-content: center; }
  .admin-main { padding: 56px 12px 16px; }
}
</style>

<style>
.mobile-drawer .el-drawer__body { padding: 0; }
</style>
