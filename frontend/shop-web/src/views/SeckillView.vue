<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchPromotions, type Promotion } from '@/api/promotion'
import { imageOrPlaceholder } from '@/utils/image'

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
        <div class="promo-tag">{{ p.promotionType === 'FLASH_SALE' ? '秒杀' : '促销' }}</div>
        <div class="promo-image">
          <img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.productName || p.title" loading="lazy" />
        </div>
        <div class="promo-title">{{ p.title }}</div>
        <div class="promo-name">{{ p.productName || `商品 #${p.productId}` }}</div>
        <div class="promo-price">
          <span class="current">¥{{ Number(p.promotionPrice || 0).toFixed(2) }}</span>
          <span v-if="p.originalPrice && p.originalPrice > p.promotionPrice" class="original">¥{{ Number(p.originalPrice).toFixed(2) }}</span>
        </div>
        <div class="promo-meta">
          <span>进入详情选择规格</span>
          <span v-if="p.promotionStock !== null && p.promotionStock !== undefined">库存 {{ p.promotionStock }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }
.promo-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.promo-card { background: #fff; border-radius: 12px; padding: 20px; cursor: pointer; position: relative; box-shadow: 0 1px 4px rgba(0,0,0,0.04); transition: all 0.2s; min-height: 260px; }
.promo-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.promo-tag { position: absolute; top: 0; left: 0; background: #ff0036; color: #fff; font-size: 11px; padding: 2px 10px; border-radius: 12px 0 8px 0; }
.promo-image { height: 126px; display: flex; align-items: center; justify-content: center; margin-bottom: 12px; }
.promo-image img { max-width: 100%; max-height: 120px; object-fit: contain; }
.promo-title { font-size: 13px; color: #ff0036; font-weight: 700; margin-bottom: 6px; }
.promo-name { font-size: 14px; font-weight: 600; margin: 0 0 12px; color: #111827; line-height: 1.4; min-height: 40px; }
.promo-price .current { font-size: 22px; font-weight: 700; color: var(--color-price, #ff0036); }
.promo-price .original { margin-left: 8px; color: #9ca3af; font-size: 13px; text-decoration: line-through; }
.promo-meta { display: flex; justify-content: space-between; gap: 8px; margin-top: 10px; color: #6b7280; font-size: 12px; }
@media (max-width: 768px) { .promo-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
