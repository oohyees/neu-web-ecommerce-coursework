<template>
  <ShopLayout>
    <div class="page-wrap user-layout">
      <aside class="side-menu">
        <div class="user-card">
          <el-avatar :size="52" :src="userInfo.avatarUrl || ''" />
          <div>
            <strong>{{ userStore.nickname || '未设置昵称' }}</strong>
            <span>{{ userInfo.email || '未绑定邮箱' }}</span>
          </div>
        </div>
        <nav>
          <router-link v-for="item in menuItems" :key="item.key" :to="item.path" custom v-slot="{ href, navigate, isActive }">
            <a :href="href" :class="{ active: isActive }" @click="navigate">
              <span class="menu-icon">{{ item.icon }}</span>
              {{ item.label }}
            </a>
          </router-link>
        </nav>
      </aside>
      <main class="content-area">
        <router-view />
      </main>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../api/index'
import { useUserStore } from '../stores/user'
import ShopLayout from './ShopLayout.vue'

const userStore = useUserStore()
const userInfo = ref<Record<string, any>>({})

const menuItems = [
  { key: 'profile', label: '个人资料', icon: '👤', path: '/user/profile' },
  { key: 'orders', label: '我的订单', icon: '📋', path: '/user/orders' },
  { key: 'addresses', label: '收货地址', icon: '📍', path: '/user/addresses' },
  { key: 'favorites', label: '我的收藏', icon: '❤️', path: '/user/favorites' },
  { key: 'coupons', label: '我的优惠券', icon: '🎫', path: '/user/coupons' },
  { key: 'security', label: '账户安全', icon: '🔒', path: '/user/security' }
]

onMounted(async () => {
  if (userStore.userId) {
    userInfo.value = (await api.get('/auth/profile', { params: { userId: userStore.userId } })).data.data || {}
  }
})
</script>

<style scoped>
.user-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
  min-height: 60vh;
}

.side-menu {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  overflow: hidden;
  align-self: start;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 16px;
  background: linear-gradient(135deg, #fff5f6, #fff);
  border-bottom: 1px solid var(--line);
}

.user-card div {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.user-card strong {
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-card span {
  color: var(--muted);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.side-menu nav {
  display: grid;
  padding: 8px 0;
}

.side-menu a {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 20px;
  text-decoration: none;
  font-size: 14px;
  color: var(--ink);
  transition: color .2s, background .2s;
}

.side-menu a:hover {
  color: var(--brand);
  background: var(--brand-light);
}

.side-menu a.active {
  color: var(--brand);
  background: var(--brand-light);
  font-weight: 650;
  border-right: 3px solid var(--brand);
}

.menu-icon {
  font-size: 16px;
  width: 22px;
  text-align: center;
}

.content-area {
  min-width: 0;
}

@media (max-width: 768px) {
  .user-layout {
    grid-template-columns: 1fr;
  }
  .side-menu {
    position: sticky;
    top: 0;
    z-index: 5;
  }
  .side-menu nav {
    display: flex;
    flex-wrap: wrap;
    gap: 0;
    padding: 4px;
  }
  .side-menu a {
    width: auto;
    padding: 10px 14px;
    font-size: 13px;
  }
  .side-menu a.active {
    border-right: none;
    border-bottom: 2px solid var(--brand);
  }
  .user-card { display: none; }
}
</style>
