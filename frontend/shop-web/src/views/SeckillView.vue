<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchPromotions, type Promotion } from '@/api/promotion'

const router = useRouter()
const promotions = ref<Promotion[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res: any = await fetchPromotions()
    promotions.value = res.data ?? []
  } catch { /* empty */ }
  finally { loading.value = false }
})
</script>

<template>
  <div class="page-container">
    <div class="page-intro">
      <div>
        <h2 class="page-title">限时秒杀</h2>
        <p>展示促销活动商品，点击后进入详情页继续选择规格、加购和下单。</p>
      </div>
    </div>
    <div class="page-metrics">
      <div class="metric-card"><span>活动数量</span><strong>{{ promotions.length }}</strong><small>当前秒杀列表</small></div>
      <div class="metric-card"><span>价格展示</span><strong>促销价</strong><small>对比原价</small></div>
      <div class="metric-card"><span>后台联动</span><strong>促销管理</strong><small>配置活动</small></div>
    </div>
    <div v-if="loading"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="!promotions.length" class="empty"><el-empty description="暂无秒杀活动" /></div>
    <div v-else class="promo-grid">
      <div v-for="p in promotions" :key="p.id" class="promo-card" @click="router.push(`/product/${p.productId}`)">
        <div class="promo-tag">秒杀</div>
        <div class="promo-name">{{ p.productName }}</div>
        <div class="promo-price">
          <span class="current">¥{{ p.discountPrice }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }
.promo-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.promo-card { background: #fff; border-radius: 12px; padding: 20px; cursor: pointer; position: relative; box-shadow: 0 1px 4px rgba(0,0,0,0.04); transition: all 0.2s; }
.promo-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.promo-tag { position: absolute; top: 0; left: 0; background: #ff0036; color: #fff; font-size: 11px; padding: 2px 10px; border-radius: 12px 0 8px 0; }
.promo-name { font-size: 14px; font-weight: 500; margin: 8px 0 12px; }
.promo-price .current { font-size: 22px; font-weight: 700; color: var(--color-price, #ff0036); }
@media (max-width: 768px) { .promo-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
