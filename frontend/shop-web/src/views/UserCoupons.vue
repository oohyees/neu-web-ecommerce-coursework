<template>
  <section class="page-card">
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
          <el-button size="small" type="danger" plain @click="$router.push('/products')">去使用</el-button>
        </div>
      </article>
    </div>
    <EmptyState v-else title="暂无优惠券" description="领取优惠券后可在此查看。" />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '@/api'
import { useUserStore, useCartStore, useFavoriteStore } from '@/stores'
import EmptyState from '../components/EmptyState.vue'

const userStore = useUserStore()
const coupons = ref<any[]>([])

onMounted(async () => {
  coupons.value = (await api.get(`/marketing/coupons/user/${userStore.userId}`)).data.data || []
})
</script>

<style scoped>
.card-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 700;
}

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
  .coupon-grid { grid-template-columns: 1fr; }
}
</style>
