<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMyCoupons, fetchAvailableCoupons, claimCoupon, normalizeCoupon } from '@/api/coupon'

const myCoupons = ref<any[]>([])
const available = ref<any[]>([])

async function load() {
  try {
    const [my, all] = await Promise.all([fetchMyCoupons(), fetchAvailableCoupons()])
    myCoupons.value = ((my as any).data ?? []).map(normalizeCoupon)
    available.value = ((all as any).data ?? []).map(normalizeCoupon)
  } catch { /* handled */ }
}

async function handleClaim(couponId: number) {
  try {
    await claimCoupon(couponId)
    ElMessage.success('领取成功')
    load()
  } catch { /* handled */ }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-intro">
      <div>
        <h2 class="page-title">我的优惠券</h2>
        <p>展示可领取和已领取优惠券，结算页会校验门槛并抵扣金额。</p>
      </div>
    </div>
    <div class="page-metrics">
      <div class="metric-card"><span>可领取</span><strong>{{ available.length }}</strong><small>平台优惠</small></div>
      <div class="metric-card"><span>我的券</span><strong>{{ myCoupons.length }}</strong><small>用户资产</small></div>
      <div class="metric-card"><span>结算联动</span><strong>支持</strong><small>下单抵扣</small></div>
    </div>
    <div v-if="available.length" class="section">
      <h3>可领取</h3>
      <div class="coupon-list">
        <div v-for="c in available" :key="c.id" class="coupon-card">
          <div class="coupon-amount">¥{{ c.value }}</div>
          <div class="coupon-cond">满{{ c.minAmount }}可用</div>
          <el-button size="small" type="primary" @click="handleClaim(c.id)">领取</el-button>
        </div>
      </div>
    </div>
    <div class="section">
      <h3>已领取</h3>
      <div v-if="myCoupons.length" class="coupon-list">
        <div v-for="c in myCoupons" :key="c.id" class="coupon-card used">
          <div class="coupon-amount">¥{{ c.value }}</div>
          <div class="coupon-cond">{{ c.status === 'USED' ? '已使用' : '未使用' }}</div>
        </div>
      </div>
      <div v-else class="empty">
        <el-empty description="暂无优惠券">
          <router-link to="/products" class="empty-link">去挑选可用商品</router-link>
        </el-empty>
      </div>
    </div>
  </div>
</template>
<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; }
.section { margin-bottom: 24px; }
.section h3 { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.coupon-list { display: flex; flex-wrap: wrap; gap: 12px; }
.coupon-card { background: #fff; border-radius: 12px; padding: 16px 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.coupon-card.used { opacity: 0.5; }
.coupon-amount { font-size: 24px; font-weight: 700; color: var(--color-price, #ff0036); }
.coupon-cond { font-size: 13px; color: #888; }
.empty-link { color: var(--color-primary); font-weight: 700; font-size: 13px; }
</style>
