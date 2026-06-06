<template>
  <ShopLayout>
    <div class="page-wrap">
      <div class="seckill-hero">
        <div class="seckill-hero-content">
          <span class="seckill-hero-badge">⚡ 限时秒杀</span>
          <h1>爆款直降 · 抢完即止</h1>
          <p>精选好物限时特惠，手慢无！</p>
        </div>
      </div>

      <section v-if="promotions.length" class="seckill-grid">
        <article
          v-for="promo in promotions"
          :key="promo.id"
          class="seckill-card"
          @click="goProduct(promo)"
        >
          <div class="seckill-card-img">
            <img v-if="promo.imageUrl" :src="promo.imageUrl" :alt="promo.title" @error="imgFallback" />
            <div v-else class="seckill-card-placeholder">{{ promo.title?.slice(0, 2) || '秒杀' }}</div>
            <span class="seckill-card-tag">秒杀</span>
          </div>
          <div class="seckill-card-body">
            <strong>{{ promo.title }}</strong>
            <p>{{ promo.description || '限时特惠，数量有限' }}</p>
            <div class="seckill-card-products" v-if="promo.products?.length">
              <div v-for="p in promo.products.slice(0, 3)" :key="p.id" class="seckill-product-mini">
                <span>{{ p.name }}</span>
                <b>¥{{ p.promotionPrice || p.price }}</b>
              </div>
            </div>
          </div>
        </article>
      </section>

      <EmptyState v-else title="暂无秒杀活动" description="敬请关注后续秒杀活动。">
        <el-button type="danger" @click="$router.push('/products')">去逛逛</el-button>
      </EmptyState>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { Promotion, Product } from '@/types'
import { getPromotions } from '@/api/marketing'
import ShopLayout from '@/layouts/ShopLayout.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const promotions = ref<Promotion[]>([])

function goProduct(promo: Promotion) {
  const firstProduct = promo.products?.[0] as Product | undefined
  if (firstProduct) {
    router.push(`/products/${firstProduct.id}`)
  } else {
    router.push('/products?sort=sales_desc')
  }
}

function imgFallback(e: Event) {
  (e.target as HTMLImageElement).src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 200"><rect fill="%23f3f4f6" width="400" height="200"/><text x="200" y="100" text-anchor="middle" dy=".35em" fill="%239ca3af" font-size="16">暂无图片</text></svg>'
}

onMounted(async () => {
  try {
    const res = await getPromotions()
    promotions.value = res.data || []
  } catch { /* empty */ }
})
</script>

<style scoped>
.seckill-hero {
  margin-bottom: 24px;
  padding: 40px 32px;
  background: linear-gradient(135deg, #ff0036 0%, #ff5000 50%, #ff6a00 100%);
  border-radius: var(--radius-lg);
  color: #fff;
  box-shadow: 0 8px 32px rgba(255, 0, 54, 0.25);
}
.seckill-hero-content { max-width: 600px; }
.seckill-hero-badge {
  display: inline-block;
  padding: 4px 14px;
  background: rgba(255,255,255,0.2);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 12px;
  backdrop-filter: blur(4px);
}
.seckill-hero h1 { margin: 0 0 8px; font-size: 32px; font-weight: 800; }
.seckill-hero p { margin: 0; font-size: 16px; opacity: 0.9; }

.seckill-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}
.seckill-card {
  cursor: pointer;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--duration) var(--ease-out);
}
.seckill-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
.seckill-card-img {
  position: relative;
  height: 180px;
  background: linear-gradient(135deg, #fff5f6, #fff7ed);
}
.seckill-card-img img { width: 100%; height: 100%; object-fit: contain; padding: 16px; }
.seckill-card-placeholder {
  display: grid; place-items: center; height: 100%;
  color: var(--brand); font-size: 28px; font-weight: 800;
}
.seckill-card-tag {
  position: absolute; top: 10px; left: 10px;
  padding: 3px 10px; color: #fff; font-size: 11px; font-weight: 700;
  background: linear-gradient(135deg, #ff0036, #ff5000); border-radius: var(--radius-full);
}
.seckill-card-body { padding: 16px; }
.seckill-card-body strong { font-size: 16px; }
.seckill-card-body p { color: var(--muted); font-size: 13px; margin: 6px 0; }
.seckill-product-mini {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; border-top: 1px solid var(--line-soft);
  font-size: 13px;
}
.seckill-product-mini b { color: var(--brand); }

@media (max-width: 900px) { .seckill-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 540px) { .seckill-grid { grid-template-columns: 1fr; } .seckill-hero h1 { font-size: 24px; } }
</style>
