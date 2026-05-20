<template>
  <ShopLayout>
    <div class="page-wrap">
      <!-- Hero: 三栏布局 -->
      <section class="home-hero">
        <!-- 左侧分类 -->
        <aside class="category-panel">
          <h2>全部分类</h2>
          <button v-for="group in categoryGroups" :key="group.id" @click="$router.push(`/products?categoryId=${group.id}`)">
            <strong>{{ group.name }}</strong>
            <span>{{ group.children.map(c => c.name).slice(0, 3).join(' / ') || '精选好物' }}</span>
          </button>
          <p v-if="!categoryGroups.length" class="empty-tip">暂无分类，请在后台维护分类数据。</p>
        </aside>

        <!-- 中间 Banner -->
        <div class="banner-area">
          <el-carousel v-if="data.banners.length" class="hero-carousel" height="360px">
            <el-carousel-item v-for="b in data.banners" :key="b.id">
              <img :src="b.imageUrl" :alt="b.title" />
              <div class="banner-copy">
                <strong>{{ b.title || '商城精选活动' }}</strong>
                <el-button type="danger" size="small" @click="$router.push(b.linkUrl || '/products')">立即查看</el-button>
              </div>
            </el-carousel-item>
          </el-carousel>
          <section v-else class="banner-gradient">
            <div class="banner-slide">
              <div>
                <strong>精选好物 限时优惠</strong>
                <p>办公数码低至7折，全场满199包邮</p>
                <el-button type="danger" size="large" @click="$router.push('/products')">立即抢购</el-button>
              </div>
            </div>
          </section>
        </div>

        <!-- 右侧用户面板 -->
        <aside class="service-panel">
          <h2>{{ session.nickname || '欢迎选购' }}</h2>
          <p>登录后可查看订单、收藏和专属优惠。</p>
          <div class="quick-actions">
            <el-button type="danger" @click="$router.push(session.userId ? '/orders' : '/login')">我的订单</el-button>
            <el-button @click="$router.push('/cart')">购物车</el-button>
          </div>
          <el-divider />
          <strong class="panel-head">商城公告</strong>
          <p v-if="!announcements.length && !notices.length" class="empty-tip">暂无公告</p>
          <p v-for="a in announcements.slice(0, 2)" :key="a.id" class="notice-line">{{ a.title }}</p>
          <p v-for="n in notices.slice(0, 2)" :key="`n-${n.id}`" class="notice-line">{{ n.title }}</p>
        </aside>
      </section>

      <!-- 促销/优惠券四宫格 -->
      <section class="promo-grid" v-if="promotions.length || coupons.length">
        <article v-for="p in promotions.slice(0, 2)" :key="`p-${p.id}`" class="promo-card">
          <span class="promo-tag">促销</span>
          <strong>{{ p.title }}</strong>
          <p>{{ p.promotionType || '限时特惠' }}</p>
          <el-button link type="danger" @click="$router.push('/products')">立即抢购 →</el-button>
        </article>
        <article v-for="c in coupons.slice(0, coupons.length < 2 ? 2 : 4 - Math.min(promotions.length, 2))" :key="`c-${c.id}`" class="promo-card">
          <span class="promo-tag coupon">优惠券</span>
          <strong>{{ c.name }}</strong>
          <p>满 ¥{{ c.thresholdAmount }} 减 ¥{{ c.discountAmount }}</p>
          <el-button link type="danger" @click="claim(c.id)">立即领取 →</el-button>
        </article>
      </section>

      <!-- 热门商品 -->
      <div class="section-title">
        <h2>热门商品</h2>
        <el-button link type="danger" @click="$router.push('/products?sort=sales_desc')">查看更多 →</el-button>
      </div>
      <section class="product-grid">
        <ProductCard v-for="p in data.hotProducts" :key="p.id" :product="p" badge="热卖" @add="addToCart" @favorite="favorite" />
      </section>
      <EmptyState v-if="!data.hotProducts.length" title="暂无热门商品" description="请先在后台维护商品数据。" />

      <!-- 新品推荐 -->
      <div class="section-title">
        <h2>新品推荐</h2>
        <el-button link type="danger" @click="$router.push('/products?sort=newest')">查看更多 →</el-button>
      </div>
      <section class="product-grid">
        <ProductCard v-for="p in data.newProducts" :key="p.id" :product="p" badge="新品" @add="addToCart" @favorite="favorite" />
      </section>
      <EmptyState v-if="data.hotProducts.length && !data.newProducts.length" title="暂无新品" description="敬请期待更多新品上架。" />
    </div>
  </ShopLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useSessionStore } from '../store'
import ShopLayout from '../layouts/ShopLayout.vue'
import ProductCard from '../components/ProductCard.vue'
import EmptyState from '../components/EmptyState.vue'

const session = useSessionStore()
const data = ref({ banners: [], hotProducts: [], newProducts: [] })
const announcements = ref([])
const notices = ref([])
const promotions = ref([])
const categories = ref([])
const coupons = ref([])

const categoryGroups = computed(() => categories.value.filter(c => !c.parentId).map(parent => ({
  ...parent,
  children: categories.value.filter(c => c.parentId === parent.id)
})))

async function claim(couponId) {
  if (!session.userId) return ElMessage.warning('请先登录后领取优惠券')
  await api.post(`/marketing/coupons/${couponId}/claim`, null, { params: { userId: session.userId } })
  ElMessage.success('优惠券已领取')
}

async function addToCart(product) {
  if (!session.userId) return ElMessage.warning('请先登录后加入购物车')
  await api.post('/cart/items', { userId: session.userId, productId: product.id, quantity: 1 })
  ElMessage.success('已加入购物车')
}

async function favorite(product) {
  if (!session.userId) return ElMessage.warning('请先登录后收藏')
  await api.post(`/favorites/${product.id}`, null, { params: { userId: session.userId } })
  ElMessage.success('已收藏商品')
}

onMounted(async () => {
  data.value = (await api.get('/home')).data.data || data.value
  announcements.value = (await api.get('/announcements')).data.data || []
  notices.value = (await api.get('/activity-notices')).data.data || []
  promotions.value = (await api.get('/marketing/promotions')).data.data || []
  categories.value = (await api.get('/categories')).data.data || []
  coupons.value = (await api.get('/marketing/coupons')).data.data || []
})
</script>

<style scoped>
/* ---- hero 三栏 ---- */
.home-hero {
  display: grid;
  grid-template-columns: 228px minmax(0, 1fr) 248px;
  gap: 16px;
  margin-bottom: 20px;
}

.category-panel,
.service-panel {
  padding: 16px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

.category-panel h2,
.service-panel h2 {
  margin: 0 0 12px;
  font-size: 17px;
  font-weight: 700;
}

.category-panel button {
  display: grid;
  width: 100%;
  padding: 11px 0;
  text-align: left;
  cursor: pointer;
  background: none;
  border: 0;
  border-bottom: 1px solid #f1f5f9;
  transition: color .2s;
}

.category-panel button:hover strong {
  color: var(--brand);
}

.category-panel strong {
  font-size: 14px;
  font-weight: 600;
  transition: color .2s;
}

.category-panel span,
.service-panel p,
.empty-tip {
  color: var(--muted);
  font-size: 13px;
}

/* ---- banner ---- */
.banner-area {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.hero-carousel {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: linear-gradient(135deg, #fff7ed 0%, #fff1f2 45%, #eef2ff 100%);
}

.hero-carousel img {
  width: 100%;
  height: 100%;
  padding: 24px;
  object-fit: contain;
  background: transparent;
}

.banner-copy {
  position: absolute;
  right: 28px;
  bottom: 28px;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, .92);
  border-radius: var(--radius);
}

.banner-copy strong {
  font-size: 16px;
}

/* CSS 渐变促销 banner */
.banner-gradient {
  height: 360px;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.banner-slide {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #ff6b6b 0%, #e60023 30%, #cc0020 65%, #ff4757 100%);
  text-align: center;
  color: #fff;
}

.banner-slide strong {
  display: block;
  font-size: 36px;
  font-weight: 800;
  text-shadow: 0 2px 8px rgba(0, 0, 0, .15);
}

.banner-slide p {
  margin: 14px 0 22px;
  font-size: 18px;
  opacity: .95;
  color: #fff;
}

.banner-slide :deep(.el-button--danger) {
  padding: 12px 36px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 999px;
  background: #fff !important;
  color: var(--brand) !important;
  border-color: #fff !important;
}

.banner-slide :deep(.el-button--danger:hover) {
  background: #fff1f3 !important;
}

/* ---- 右侧用户面板 ---- */
.service-panel .panel-head {
  display: block;
  margin-bottom: 10px;
  font-size: 14px;
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 12px;
}

.notice-line {
  margin: 8px 0 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  cursor: default;
}

/* ---- 促销四宫格 ---- */
.promo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 8px;
}

.promo-card {
  min-height: 130px;
  padding: 18px;
  background: linear-gradient(135deg, #fff5f6, #fff);
  border: 1px solid #fecdd3;
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
}

.promo-tag {
  display: inline-block;
  width: fit-content;
  padding: 2px 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  background: var(--brand);
  border-radius: 999px;
  margin-bottom: 10px;
}

.promo-tag.coupon {
  background: #ff7a00;
}

.promo-card strong {
  font-size: 16px;
  margin-bottom: 6px;
}

.promo-card p {
  color: var(--muted);
  font-size: 13px;
  margin: 0 0 8px;
}

.promo-card :deep(.el-button) {
  margin-top: auto;
  align-self: flex-start;
  padding: 0;
  font-weight: 600;
}

/* ---- 商品网格 ---- */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

/* ---- 响应式 ---- */
@media (max-width: 1100px) {
  .home-hero {
    grid-template-columns: 1fr;
  }
  .promo-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .home-hero {
    grid-template-columns: 1fr;
  }
  .banner-area {
    order: -1;
  }
  .hero-carousel :deep(.el-carousel__container),
  .banner-gradient {
    height: 240px !important;
  }
  .hero-carousel img {
    padding: 18px;
  }
  .banner-copy {
    right: 14px;
    bottom: 14px;
    left: 14px;
    justify-content: space-between;
    padding: 10px 12px;
  }
  .banner-copy strong {
    font-size: 14px;
  }
  .promo-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .banner-slide strong {
    font-size: 26px;
  }
  .banner-slide p {
    font-size: 15px;
    margin: 10px 0 16px;
  }
}

@media (max-width: 540px) {
  .promo-grid {
    grid-template-columns: 1fr;
  }
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }
}
</style>
