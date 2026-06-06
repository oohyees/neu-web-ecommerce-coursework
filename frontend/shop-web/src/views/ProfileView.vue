<template>
  <ShopLayout>
    <div class="page-wrap profile-layout">
      <!-- 左侧菜单 -->
      <aside class="side-menu">
        <div class="user-card">
          <el-avatar :size="56" :src="form.avatarUrl" />
          <div>
            <strong>{{ form.nickname || '未设置昵称' }}</strong>
            <span>{{ form.email || '未绑定邮箱' }}</span>
          </div>
        </div>
        <nav>
          <button v-for="item in menuItems" :key="item.key" :class="{ active: activeMenu === item.key }" @click="switchMenu(item.key)">
            <span class="menu-icon">{{ item.icon }}</span>
            {{ item.label }}
          </button>
        </nav>
      </aside>

      <!-- 右侧内容 -->
      <main class="content-area">
        <!-- 个人资料 -->
        <section v-show="activeMenu === 'profile'" class="page-card">
          <h2 class="card-title">基本资料</h2>
          <div class="avatar-block">
            <el-avatar :size="80" :src="form.avatarUrl" />
            <el-upload :show-file-list="false" :http-request="uploadAvatar">
              <el-button size="small">更换头像</el-button>
            </el-upload>
            <span class="muted">支持 JPG、PNG 格式</span>
          </div>
          <el-form label-width="80px" class="profile-form">
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="saveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </section>

        <!-- 账户安全 -->
        <section v-show="activeMenu === 'security'" class="page-card">
          <h2 class="card-title">修改密码</h2>
          <el-form label-width="88px" class="profile-form">
            <el-form-item label="原密码">
              <el-input v-model="pwd.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwd.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwd.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </section>

        <!-- 我的优惠券 -->
        <section v-show="activeMenu === 'coupons'" class="page-card">
          <h2 class="card-title">我的优惠券</h2>
          <div v-if="coupons.length" class="coupon-grid">
            <article v-for="c in coupons" :key="c.id" class="coupon-card">
              <div class="coupon-left">
                <b>&yen;{{ c.discountAmount }}</b>
                <span>满{{ c.thresholdAmount }}可用</span>
              </div>
              <div class="coupon-right">
                <strong>{{ c.name }}</strong>
                <p>有效期至 {{ c.expireDate || '长期有效' }}</p>
                <el-button size="small" type="danger" plain>去使用</el-button>
              </div>
            </article>
          </div>
          <EmptyState v-else title="暂无优惠券" description="领取优惠券后可在此查看。" />
        </section>

        <!-- 默认提示其他菜单项 -->
        <section v-if="!['profile', 'security', 'coupons'].includes(activeMenu)" class="page-card">
          <EmptyState title="功能开发中" :description="`${currentMenuLabel}页面正在建设中，敬请期待。`" />
        </section>
      </main>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useUserStore, useCartStore, useFavoriteStore } from '@/stores'
import ShopLayout from '../layouts/ShopLayout.vue'
import EmptyState from '../components/EmptyState.vue'

const userStore = useUserStore()
const router = useRouter()
const activeMenu = ref('profile')
const form = ref<any>({})
const coupons = ref<any[]>([])
const pwd = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const menuItems = [
  { key: 'profile', label: '个人资料', icon: '👤' },
  { key: 'orders', label: '我的订单', icon: '📋', link: '/orders' },
  { key: 'addresses', label: '收货地址', icon: '📍', link: '/user/addresses' },
  { key: 'favorites', label: '我的收藏', icon: '❤️', link: '/user/favorites' },
  { key: 'coupons', label: '我的优惠券', icon: '🎫' },
  { key: 'security', label: '账户安全', icon: '🔒' }
]

const currentMenuLabel = computed(() => menuItems.find(m => m.key === activeMenu.value)?.label || '')

function switchMenu(key) {
  const item = menuItems.find(m => m.key === key)
  if (item?.link) {
    router.push(item.link)
    return
  }
  activeMenu.value = key
}

async function saveProfile() {
  await api.put('/auth/profile', form.value)
  ElMessage.success('资料已保存')
}

async function changePassword() {
  if (!pwd.value.oldPassword || !pwd.value.newPassword) return ElMessage.warning('请填写完整密码信息')
  if (pwd.value.newPassword !== pwd.value.confirmPassword) return ElMessage.warning('两次输入的新密码不一致')
  const { data } = await api.put('/auth/password', { userId: userStore.userId, oldPassword: pwd.value.oldPassword, newPassword: pwd.value.newPassword })
  if (!data.success) return ElMessage.error(data.message)
  ElMessage.success('密码已更新')
  pwd.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
}

async function uploadAvatar({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  form.value.avatarUrl = (await api.post('/files/upload', fd)).data.data
}

onMounted(async () => {
  form.value = (await api.get('/auth/profile', { params: { userId: userStore.userId } })).data.data || {}
  coupons.value = (await api.get(`/marketing/coupons/user/${userStore.userId}`)).data.data || []
})
</script>

<style scoped>
.profile-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
  min-height: 60vh;
}

/* 左侧菜单 */
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
  padding: 20px 16px;
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

.side-menu button {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 13px 20px;
  text-align: left;
  cursor: pointer;
  background: none;
  border: 0;
  font-size: 14px;
  color: var(--ink);
  transition: color .2s, background .2s;
}

.side-menu button:hover {
  color: var(--brand);
  background: var(--brand-light);
}

.side-menu button.active {
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

/* 右侧内容 */
.content-area {
  min-width: 0;
}

.card-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 700;
}

.avatar-block {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--line);
}

.profile-form {
  max-width: 420px;
}

/* 优惠券 */
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.coupon-card {
  display: flex;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}

.coupon-left {
  display: grid;
  place-content: center;
  width: 110px;
  padding: 16px;
  text-align: center;
  background: linear-gradient(135deg, var(--brand), #ff4757);
  color: #fff;
  flex-shrink: 0;
}

.coupon-left b {
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.coupon-left span {
  margin-top: 4px;
  font-size: 12px;
  opacity: .85;
}

.coupon-right {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: #fff;
}

.coupon-right strong {
  font-size: 15px;
}

.coupon-right p {
  color: var(--muted);
  font-size: 12px;
  margin: 0;
}

@media (max-width: 768px) {
  .profile-layout {
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
  .side-menu button {
    width: auto;
    padding: 10px 14px;
    font-size: 13px;
  }
  .side-menu button.active {
    border-right: none;
    border-bottom: 2px solid var(--brand);
  }
  .user-card { display: none; }
  .coupon-grid { grid-template-columns: 1fr; }
}
</style>
