<template>
  <div class="profile-overview">
    <section class="profile-hero page-card">
      <div class="avatar-block">
        <el-avatar :size="82" :src="form.avatarUrl" />
        <div class="profile-title">
          <h2>{{ form.nickname || userStore.nickname || '商城用户' }}</h2>
          <p>{{ form.email || '未绑定邮箱' }}</p>
          <el-upload :show-file-list="false" :http-request="uploadAvatar">
            <el-button size="small">更换头像</el-button>
          </el-upload>
        </div>
      </div>
      <div class="status-grid">
        <button v-for="item in statusCards" :key="item.key" type="button" @click="$router.push(item.path)">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </button>
      </div>
    </section>

    <section class="page-card">
      <div class="section-head">
        <h3>验收功能入口</h3>
        <span class="muted">订单、地址、收藏、优惠券、客服和反馈集中展示</span>
      </div>
      <div class="feature-grid">
        <button v-for="item in featureLinks" :key="item.path" type="button" @click="$router.push(item.path)">
          <span class="feature-icon">{{ item.icon }}</span>
          <strong>{{ item.label }}</strong>
          <em>{{ item.desc }}</em>
        </button>
      </div>
    </section>

    <section class="page-card">
      <div class="section-head">
        <h3>基本资料</h3>
        <span class="muted">支持头像、昵称、邮箱和手机号维护</span>
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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const form = ref<any>({})
const summary = ref<Record<string, number | string>>({
  unpaid: '-',
  shipped: '-',
  completed: '-',
  coupons: '-',
})

const statusCards = computed(() => [
  { key: 'unpaid', label: '待支付', value: summary.value.unpaid, path: '/user/orders' },
  { key: 'shipped', label: '待收货', value: summary.value.shipped, path: '/user/orders' },
  { key: 'completed', label: '已完成', value: summary.value.completed, path: '/user/orders' },
  { key: 'coupons', label: '优惠券', value: summary.value.coupons, path: '/user/coupons' },
])

const featureLinks = [
  { label: '我的订单', desc: '支付、物流、退款、评价', icon: '单', path: '/user/orders' },
  { label: '收货地址', desc: '新增、编辑、默认地址', icon: '址', path: '/user/addresses' },
  { label: '我的收藏', desc: '收藏商品集中查看', icon: '藏', path: '/user/favorites' },
  { label: '优惠券', desc: '领券后结算抵扣', icon: '券', path: '/user/coupons' },
  { label: '账户安全', desc: '密码和邮箱安全设置', icon: '安', path: '/user/security' },
  { label: '意见反馈', desc: '问题建议提交给后台', icon: '馈', path: '/feedback' },
  { label: '在线客服', desc: '咨询订单和售后问题', icon: '客', path: '/consultations' },
  { label: '活动公告', desc: '系统公告和活动通知', icon: '告', path: '/notices' },
]

async function saveProfile() {
  await api.put('/auth/profile', form.value)
  ElMessage.success('资料已保存')
}

async function uploadAvatar({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  form.value.avatarUrl = (await api.post('/files/upload', fd)).data.data
}

onMounted(async () => {
  form.value = (await api.get('/auth/profile', { params: { userId: userStore.userId } })).data.data || {}
  loadSummary()
})

async function loadOrderCount(params: Record<string, any>) {
  try {
    const result = (await api.get('/orders', { params: { userId: userStore.userId, page: 1, size: 1, ...params } })).data.data || {}
    return result.total ?? 0
  } catch {
    return '-'
  }
}

async function loadSummary() {
  if (!userStore.userId) return
  const [unpaid, shipped, completed, coupons] = await Promise.all([
    loadOrderCount({ paymentStatus: 'UNPAID' }),
    loadOrderCount({ status: 'SHIPPED' }),
    loadOrderCount({ status: 'COMPLETED' }),
    api.get('/marketing/coupons/my', { params: { userId: userStore.userId } })
      .then(res => (res.data.data || []).length)
      .catch(() => '-'),
  ])
  summary.value = { unpaid, shipped, completed, coupons }
}
</script>

<style scoped>
.profile-overview {
  display: grid;
  gap: 16px;
}

.profile-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 20px;
  align-items: center;
}

.avatar-block {
  display: flex;
  align-items: center;
  gap: 14px;
}

.profile-title {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.profile-title h2,
.profile-title p {
  margin: 0;
}

.profile-title h2 {
  font-size: 22px;
}

.profile-title p {
  color: var(--muted);
  font-size: 14px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.status-grid button {
  display: grid;
  gap: 4px;
  padding: 12px;
  cursor: pointer;
  text-align: left;
  background: #fff7f8;
  border: 1px solid #fecdd3;
  border-radius: var(--radius);
}

.status-grid span {
  color: var(--muted);
  font-size: 12px;
}

.status-grid strong {
  color: var(--brand);
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 18px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.feature-grid button {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 3px 10px;
  align-items: center;
  min-height: 78px;
  padding: 14px;
  text-align: left;
  cursor: pointer;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  transition: border-color .18s ease, box-shadow .18s ease, transform .18s ease;
}

.feature-grid button:hover {
  border-color: #f2b8c2;
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.feature-icon {
  grid-row: span 2;
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  color: var(--brand);
  background: var(--brand-light);
  border-radius: 12px;
  font-weight: 800;
}

.feature-grid strong {
  min-width: 0;
  font-size: 14px;
  line-height: 1.2;
}

.feature-grid em {
  min-width: 0;
  color: var(--muted);
  font-size: 12px;
  font-style: normal;
  line-height: 1.25;
}

.profile-form {
  max-width: 420px;
}

@media (max-width: 1100px) {
  .profile-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .section-head {
    align-items: flex-start;
    flex-direction: column;
  }
  .status-grid,
  .feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .feature-grid button {
    min-height: 72px;
    padding: 12px;
  }
}

@media (max-width: 420px) {
  .status-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
