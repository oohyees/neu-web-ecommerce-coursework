<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { fetchHomeData, trackSearch } from '@/api/notice'
import { type Product, fetchCategories, type Category } from '@/api/product'
import { imageOrPlaceholder } from '@/utils/image'

const router = useRouter()
const searchKeyword = ref('')
const placeholderIndex = ref(0)

// ═══ 首页数据 ═══
const banners = ref<{ id: number; imageUrl: string; linkUrl: string; title?: string; relatedProducts?: any[] }[]>([])
const hotProducts = ref<Product[]>([])
const newProducts = ref<Product[]>([])
const hotSearches = ref<string[]>([])
const categories = ref<Category[]>([])
const promotions = ref<any[]>([])
const coupons = ref<any[]>([])
const reviews = ref<any[]>([])
const hoverCatId = ref<number | null>(null)

// 旋转搜索占位符
const rotatingPlaceholders = computed(() => {
  const fromHot = hotSearches.value
  const fromProducts = hotProducts.value.slice(0, 6).map((p) => p.name)
  return [...fromHot, ...fromProducts].filter(Boolean)
})
const currentPlaceholder = computed(() => {
  const list = rotatingPlaceholders.value
  if (!list.length) return '搜索你想要的商品...'
  return list[placeholderIndex.value % list.length]
})

// 品牌推荐：从热门商品中提取品牌
const brands = computed(() => {
  const brandMap = new Map<string, { name: string; count: number; image: string }>()
  for (const p of hotProducts.value) {
    const brand = p.brand || p.name?.split(' ')[0] || 'Brand'
    if (!brandMap.has(brand)) {
      brandMap.set(brand, { name: brand, count: 0, image: p.imageUrl || '' })
    }
    brandMap.get(brand)!.count++
  }
  return Array.from(brandMap.values()).slice(0, 12)
})

// 限时特惠：FLASH_SALE 类型促销
const flashSales = computed(() => promotions.value.filter(p => p.promotionType === 'FLASH_SALE'))
// 普通促销
const normalPromotions = computed(() => promotions.value.filter(p => p.promotionType !== 'FLASH_SALE'))

async function loadHome() {
  try {
    const [homeRes, catRes] = await Promise.all([
      fetchHomeData(),
      fetchCategories(),
    ])
    const data: any = homeRes
    banners.value = data.banners || []
    hotProducts.value = data.hotProducts || []
    newProducts.value = data.newProducts || []
    hotSearches.value = (data.hotSearches || []).map((h: any) => h.keyword || h)
    promotions.value = data.promotions || []
    coupons.value = data.coupons || []
    reviews.value = data.reviews || []
    categories.value = catRes
  } catch { /* 静默降级 */ }
}

function goSearch(kw?: string) {
  const q = (kw || searchKeyword.value || currentPlaceholder.value).trim()
  if (q) {
    trackSearch(q)
    router.push({ path: '/search', query: { keyword: q } })
  }
}

function goSearchTag(kw: string) {
  trackSearch(kw)
  router.push({ path: '/search', query: { keyword: kw } })
}

function goProduct(id: number) {
  router.push(`/product/${id}`)
}

function goCategory(id: number) {
  router.push(`/category/${id}`)
}

function goBanner(linkUrl?: string) {
  if (!linkUrl) return
  if (linkUrl.startsWith('/product/')) {
    const id = linkUrl.split('/product/')[1]
    if (id) router.push(`/product/${id}`)
  } else if (linkUrl.startsWith('/')) {
    router.push(linkUrl)
  }
}

function formatSales(n?: number) {
  if (!n) return '0'
  if (n >= 10000) return `${(n / 10000).toFixed(1)}万+`
  if (n >= 100) return `${n}+`
  return String(n)
}

onMounted(() => {
  loadHome()
  setInterval(() => {
    const len = rotatingPlaceholders.value.length
    if (len) placeholderIndex.value = (placeholderIndex.value + 1) % len
  }, 2800)
})
</script>

<template>
  <div class="home-page">

    <!-- ═══ 搜索顶栏 ═══ -->
    <section class="search-header">
      <div class="search-top">
        <div class="brand" @click="router.push('/')">
          <span class="brand-main">优品</span>
          <span class="brand-divider">|</span>
          <span class="brand-sub">品质生活</span>
        </div>
        <div class="search-area">
          <div class="search-box">
            <input
              v-model="searchKeyword"
              :placeholder="currentPlaceholder"
              class="search-input"
              @keydown.enter="goSearch()"
            />
            <button class="search-btn" type="button" @click="goSearch()">搜索</button>
          </div>
          <div v-if="hotSearches.length" class="hot-keywords">
            <span class="label">热搜</span>
            <a v-for="kw in hotSearches.slice(0, 8)" :key="kw" href="#" @click.prevent="goSearch(kw)">{{ kw }}</a>
          </div>
        </div>
        <nav class="header-nav">
          <router-link to="/cart">购物车</router-link>
          <router-link to="/orders">我的订单</router-link>
          <router-link to="/seckill">限时秒杀</router-link>
        </nav>
      </div>
    </section>

    <!-- ═══ 主体内容 ═══ -->
    <div class="home-body">

      <!-- ═══ 轮播 + 分类侧边栏联动 ═══ -->
      <section class="hero-row">
        <div class="banner-wrap">
          <el-carousel v-if="banners.length" height="380px" indicator-position="outside" :interval="5000">
            <el-carousel-item v-for="b in banners" :key="b.id">
              <div class="banner-slide">
                <!-- 三合一构图：左小 + 中大 + 右小 -->
                <div class="composite-layout">
                  <div v-if="b.relatedProducts && b.relatedProducts.length > 0" class="side-product left-side" @click="goProduct(b.relatedProducts[0].id)">
                    <img :src="imageOrPlaceholder(b.relatedProducts[0].imageUrl)" :alt="b.relatedProducts[0].name" loading="lazy" />
                    <div class="side-label">
                      <span class="side-name">{{ b.relatedProducts[0].name }}</span>
                      <span class="side-price">¥{{ b.relatedProducts[0].price }}</span>
                    </div>
                  </div>
                  <div class="center-product" @click="goBanner(b.linkUrl)">
                    <img :src="b.imageUrl" :alt="b.title || '广告'" loading="lazy" />
                    <div class="center-overlay">
                      <div v-if="b.title" class="banner-title">{{ b.title }}</div>
                      <div class="promo-badges">
                        <span v-for="p in normalPromotions.slice(0, 2)" :key="p.id" class="badge-item" @click.stop="goProduct(p.productId)">
                          <span class="badge-name">{{ p.title }}</span>
                          <span class="badge-price">¥{{ p.promotionPrice }}</span>
                        </span>
                      </div>
                      <div v-if="coupons.length" class="banner-coupons">
                        <div v-for="c in coupons.slice(0, 3)" :key="c.id" class="mini-coupon" @click.stop>
                          <span class="mc-amount">¥{{ c.discountAmount }}</span>
                          <span class="mc-threshold">满{{ c.thresholdAmount }}可用</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-if="b.relatedProducts && b.relatedProducts.length > 1" class="side-product right-side" @click="goProduct(b.relatedProducts[1].id)">
                    <img :src="imageOrPlaceholder(b.relatedProducts[1].imageUrl)" :alt="b.relatedProducts[1].name" loading="lazy" />
                    <div class="side-label">
                      <span class="side-name">{{ b.relatedProducts[1].name }}</span>
                      <span class="side-price">¥{{ b.relatedProducts[1].price }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div v-else class="banner-placeholder">轮播加载中...</div>
        </div>
        <aside v-if="categories.length" class="category-sidebar" @mouseleave="hoverCatId = null">
          <div class="sidebar-header">全部分类</div>
          <ul class="l1-list">
            <li
              v-for="cat in categories.filter(c => !c.parentId)"
              :key="cat.id"
              class="l1-item"
              @mouseenter="hoverCatId = cat.id"
              @click="goCategory(cat.id)"
            >
              <span class="name">{{ cat.name }}</span>
              <span class="arrow">&#8249;</span>
              <div v-if="hoverCatId === cat.id && cat.children?.length" class="l2-panel">
                <a
                  v-for="sub in cat.children"
                  :key="sub.id"
                  class="l2-item"
                  @click.stop="goCategory(sub.id)"
                >
                  {{ sub.name }}
                </a>
              </div>
            </li>
          </ul>
        </aside>
      </section>

      <!-- ═══ 秒杀入口横幅 ═══ -->
      <div v-if="flashSales.length" class="seckill-entry" @click="router.push('/seckill')">
        <span class="seckill-tag">限时秒杀</span>
        <span class="seckill-desc">爆款直降 · 抢完即止</span>
        <span class="seckill-go">立即抢购 →</span>
      </div>

      <!-- ═══ 热门好物 - 横向滚动 ═══ -->
      <section v-if="hotProducts.length" class="scroll-section">
        <div class="section-title">
          <h2>热门好物</h2>
          <router-link to="/products?sort=sales_desc">查看更多 ›</router-link>
        </div>
        <div class="scroll-track">
          <div
            v-for="p in hotProducts.slice(0, 12)"
            :key="p.id"
            class="product-card"
            @click="goProduct(p.id)"
          >
            <div class="img-wrap">
              <img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.name" loading="lazy" />
              <span class="tag tag-hot">热门</span>
            </div>
            <div class="card-body">
              <h3 class="title">{{ p.name }}</h3>
              <div class="price-row">
                <span class="price"><small>¥</small>{{ p.price }}</span>
                <span v-if="p.originalPrice && p.originalPrice > p.price" class="orig">¥{{ p.originalPrice }}</span>
              </div>
              <div class="meta">
                <span>已售{{ formatSales(p.sales) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══ 促销活动 ═══ -->
      <section v-if="normalPromotions.length" class="promo-section">
        <div class="section-title">
          <h2>促销活动</h2>
          <span class="section-count">{{ normalPromotions.length }}项优惠进行中</span>
        </div>
        <div class="promo-scroll">
          <div v-for="p in normalPromotions" :key="p.id" class="promo-card" @click="goProduct(p.productId)">
            <div class="promo-img-box">
              <img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.productName" loading="lazy" />
            </div>
            <div class="promo-info">
              <span class="promo-tag">{{ p.title }}</span>
              <h3>{{ p.productName }}</h3>
              <div class="promo-prices">
                <span class="promo-price">¥{{ p.promotionPrice }}</span>
                <span class="promo-original">¥{{ p.originalPrice }}</span>
              </div>
              <button class="promo-btn" @click.stop="goProduct(p.productId)">立即抢购</button>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══ 领券中心 ═══ -->
      <section v-if="coupons.length" class="coupon-section">
        <div class="section-title">
          <h2>领券中心</h2>
          <span class="section-count">{{ coupons.length }}张优惠券可领</span>
        </div>
        <div class="coupon-scroll">
          <div v-for="c in coupons" :key="c.id" class="coupon-card">
            <div class="coupon-left">
              <span class="coupon-symbol">¥</span>
              <span class="coupon-amount">{{ c.discountAmount }}</span>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ c.name }}</div>
              <div class="coupon-threshold">满{{ c.thresholdAmount }}元可用</div>
              <button class="coupon-btn">立即领取</button>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══ 新品首发 - 横向滚动 ═══ -->
      <section v-if="newProducts.length" class="scroll-section">
        <div class="section-title">
          <h2>新品首发</h2>
          <router-link to="/products?sort=newest">查看更多 ›</router-link>
        </div>
        <div class="scroll-track">
          <div
            v-for="p in newProducts.slice(0, 12)"
            :key="p.id"
            class="product-card"
            @click="goProduct(p.id)"
          >
            <div class="img-wrap">
              <img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.name" loading="lazy" />
              <span class="tag tag-new">新品</span>
            </div>
            <div class="card-body">
              <h3 class="title">{{ p.name }}</h3>
              <div class="price-row">
                <span class="price"><small>¥</small>{{ p.price }}</span>
                <span v-if="p.originalPrice && p.originalPrice > p.price" class="orig">¥{{ p.originalPrice }}</span>
              </div>
              <div class="meta">
                <span>已售{{ formatSales(p.sales) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══ 品牌推荐 ═══ -->
      <section v-if="brands.length" class="brand-section">
        <div class="section-title">
          <h2>品牌推荐</h2>
        </div>
        <div class="brand-grid">
          <div v-for="b in brands" :key="b.name" class="brand-card" @click="goSearchTag(b.name)">
            <img :src="imageOrPlaceholder(b.image)" :alt="b.name" class="brand-img" />
            <span class="brand-name">{{ b.name }}</span>
          </div>
        </div>
      </section>

      <!-- ═══ 用户评价 ═══ -->
      <section v-if="reviews.length" class="review-section">
        <div class="section-title">
          <h2>用户评价</h2>
        </div>
        <div class="review-grid">
          <div v-for="r in reviews" :key="r.id" class="review-card" @click="goProduct(r.productId)">
            <div class="review-header">
              <div class="review-stars">
                <span v-for="s in 5" :key="s" class="review-star" :class="{ active: s <= r.rating }">★</span>
              </div>
              <span class="review-date">{{ (r.createdAt || '').slice(0, 10) }}</span>
            </div>
            <p class="review-content">{{ r.content }}</p>
            <div class="review-product">
              <img :src="imageOrPlaceholder(r.productImage)" class="review-product-img" />
              <span>{{ r.productName }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══ 底部 ═══ -->
      <footer class="home-footer">
        <p>© 2026 优品商城 · 课程演示项目</p>
      </footer>
    </div>
  </div>
</template>

<style scoped>
/* ═══ 页面容器 ═══ */
.home-page {
  min-height: 100vh;
  background: var(--color-bg, #f4f4f5);
}

/* ═══ 搜索顶栏 ═══ */
.search-header {
  background: linear-gradient(180deg, #fff 0%, #fffaf7 55%, #fff 100%);
  padding: 28px 0 20px;
  border-bottom: 1px solid rgba(255, 107, 53, 0.08);
}
.search-top {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: flex-end;
  gap: 28px;
}
.brand {
  flex-shrink: 0;
  cursor: pointer;
  padding-bottom: 8px;
}
.brand-main {
  font-size: 40px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 3px;
}
.brand-divider {
  margin: 0 10px;
  color: #e0e0e0;
  font-weight: 200;
  -webkit-text-fill-color: #e0e0e0;
}
.brand-sub {
  font-size: 17px;
  color: #555;
  font-weight: 600;
  -webkit-text-fill-color: #555;
}
.search-area {
  flex: 1;
  max-width: 640px;
  padding-bottom: 4px;
}
.search-box {
  display: flex;
  height: 48px;
  border-radius: 26px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.12);
  border: 2px solid var(--color-primary);
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 22px;
  font-size: 15px;
  background: transparent;
}
.search-btn {
  border: none;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: filter 0.2s;
}
.search-btn:hover {
  filter: brightness(1.08);
}
.hot-keywords {
  margin-top: 12px;
  font-size: 13px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 0;
}
.hot-keywords .label {
  color: var(--color-primary);
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
}
.hot-keywords a {
  margin-right: 18px;
  color: #666;
  transition: color 0.2s;
}
.hot-keywords a:hover {
  color: var(--color-primary);
}
.header-nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 18px;
  flex-shrink: 0;
  padding-bottom: 12px;
}
.header-nav a {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  transition: color 0.2s;
}
.header-nav a:hover {
  color: var(--color-primary);
}

/* ═══ 主体 ═══ */
.home-body {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 20px 0;
}

/* ═══ 轮播 + 分类侧边栏联动 ═══ */
.hero-row {
  display: flex;
  gap: 14px;
  align-items: stretch;
}
.banner-wrap {
  flex: 1;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}
.banner-wrap :deep(.el-carousel__container) {
  height: 380px !important;
}
.banner-slide {
  height: 380px;
  background: #f5f5f5;
  position: relative;
  overflow: hidden;
}

/* 三合一构图：左小 + 中大 + 右小 */
.composite-layout {
  display: flex;
  height: 100%;
  gap: 0;
}
.center-product {
  flex: 3;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  background: #f0f0f0;
}
.center-product img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 0.5s ease;
}
.center-product:hover img {
  transform: scale(1.03);
}
.center-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px 24px 16px;
  background: linear-gradient(transparent 0%, rgba(0,0,0,0.55) 100%);
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}
.center-overlay > * {
  pointer-events: auto;
}
.side-product {
  flex: 1;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  background: #fafafa;
  display: flex;
  flex-direction: column;
}
.side-product img {
  width: 100%;
  flex: 1;
  object-fit: cover;
  object-position: center;
  transition: transform 0.35s ease;
}
.side-product:hover img {
  transform: scale(1.05);
}
.side-label {
  padding: 8px 10px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 2px;
  border-top: 1px solid #f0f0f0;
}
.side-name {
  font-size: 12px;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.side-price {
  font-size: 14px;
  color: var(--color-price, #ff0036);
  font-weight: 700;
}
.left-side {
  border-right: 2px solid #fff;
}
.right-side {
  border-left: 2px solid #fff;
}

.banner-title {
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  text-shadow: 0 1px 4px rgba(0,0,0,0.3);
}
.promo-badges {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.badge-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.92);
  border-radius: 20px;
  padding: 5px 14px;
  cursor: pointer;
  transition: all 0.2s;
  backdrop-filter: blur(4px);
}
.badge-item:hover {
  background: #fff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.badge-name {
  font-size: 12px;
  color: #333;
  font-weight: 600;
}
.badge-price {
  font-size: 14px;
  color: var(--color-price, #ff0036);
  font-weight: 700;
}
.banner-coupons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.mini-coupon {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  border-radius: 6px;
  padding: 4px 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.mini-coupon:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
}
.mc-amount {
  color: #fff;
  font-size: 15px;
  font-weight: 800;
}
.mc-threshold {
  color: rgba(255,255,255,0.9);
  font-size: 11px;
}

.banner-placeholder {
  height: 380px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
}

.category-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.72);
  border-radius: 12px;
  color: #fff;
  position: relative;
  z-index: 10;
  overflow: visible;
  display: flex;
  flex-direction: column;
  max-height: 420px;
}
.sidebar-header {
  padding: 14px 16px 10px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255,255,255,0.12);
  flex-shrink: 0;
}
.l1-list {
  list-style: none;
  padding: 6px 0;
  overflow-y: auto;
  flex: 1;
}
.l1-list::-webkit-scrollbar {
  width: 4px;
}
.l1-list::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.2);
  border-radius: 2px;
}
.l1-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 16px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.15s;
}
.l1-item:hover {
  background: var(--color-primary);
}
.l1-item .name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.arrow {
  opacity: 0.6;
  font-size: 16px;
  flex-shrink: 0;
  margin-left: 8px;
}
.l2-panel {
  position: absolute;
  right: 100%;
  top: 0;
  min-width: 480px;
  min-height: 100%;
  background: #fff;
  color: var(--color-text, #1a1a1a);
  border-radius: 12px 0 0 12px;
  box-shadow: -4px 0 16px rgba(0, 0, 0, 0.08);
  padding: 20px 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  align-content: flex-start;
}
.l2-item {
  font-size: 14px;
  color: #666;
  cursor: pointer;
  padding: 4px 0;
  transition: color 0.2s;
}
.l2-item:hover {
  color: var(--color-primary);
}

/* ═══ 秒杀入口 ═══ */
.seckill-entry {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  margin: 16px 0 0;
  background: linear-gradient(90deg, #ff0036, #ff5000);
  border-radius: 12px;
  color: #fff;
  cursor: pointer;
  transition: filter 0.2s;
}
.seckill-entry:hover {
  filter: brightness(1.05);
}
.seckill-tag {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
}
.seckill-desc {
  flex: 1;
  font-size: 14px;
  opacity: 0.95;
}
.seckill-go {
  font-size: 14px;
  font-weight: 600;
}

/* ═══ 横向滚动商品区 ═══ */
.scroll-section {
  margin-top: 8px;
}
.scroll-track {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
}
.scroll-track::-webkit-scrollbar {
  height: 6px;
}
.scroll-track::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

/* ═══ 商品卡片 ═══ */
.product-card {
  width: 220px;
  flex: 0 0 220px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  border: 1px solid rgba(0, 0, 0, 0.04);
  scroll-snap-align: start;
}
.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(255, 80, 0, 0.12);
}
.img-wrap {
  position: relative;
  aspect-ratio: 1;
  background: linear-gradient(165deg, #fafafa 0%, #f0f0f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  overflow: hidden;
}
.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
  transform: scale(1.08);
  transition: transform 0.3s ease;
}
.product-card:hover .img-wrap img {
  transform: scale(1.04);
}
.tag {
  position: absolute;
  top: 10px;
  left: 10px;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 20px;
  color: #fff;
  font-weight: 500;
  z-index: 1;
}
.tag-hot {
  background: linear-gradient(135deg, #ff4d4f, #cf1322);
}
.tag-new {
  background: linear-gradient(135deg, #52c41a, #389e0d);
}
.card-body {
  padding: 12px 14px 16px;
}
.title {
  font-size: 14px;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-weight: 500;
  color: var(--color-text, #1a1a1a);
}
.price-row {
  margin-top: 10px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.price {
  color: var(--color-price, #ff0036);
  font-size: 22px;
  font-weight: 700;
  font-family: 'DIN Alternate', 'Arial Narrow', sans-serif;
}
.price small {
  font-size: 14px;
  margin-right: 1px;
}
.orig {
  font-size: 12px;
  color: #c0c0c0;
  text-decoration: line-through;
}
.meta {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-secondary, #888);
  display: flex;
  gap: 10px;
}

/* ═══ 促销活动 ═══ */
.promo-section {
  margin-top: 8px;
}
.section-count {
  font-size: 13px;
  color: #999;
  font-weight: 400;
}
.promo-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
}
.promo-scroll::-webkit-scrollbar {
  height: 6px;
}
.promo-scroll::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}
.promo-card {
  display: flex;
  width: 320px;
  flex: 0 0 320px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
  scroll-snap-align: start;
}
.promo-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
.promo-img-box {
  width: 120px;
  min-height: 120px;
  background: #f8f8f8;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.promo-img-box img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.promo-info {
  padding: 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}
.promo-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 6px;
  background: linear-gradient(135deg, var(--color-primary), #ff7a45);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  width: fit-content;
}
.promo-info h3 {
  font-size: 14px;
  font-weight: 700;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.promo-prices {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.promo-price {
  font-size: 18px;
  font-weight: 800;
  color: var(--color-primary);
}
.promo-original {
  font-size: 13px;
  color: #bbb;
  text-decoration: line-through;
}
.promo-btn {
  border: none;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  width: fit-content;
  transition: filter 0.2s;
}
.promo-btn:hover {
  filter: brightness(1.1);
}

/* ═══ 优惠券 ═══ */
.coupon-section {
  margin-top: 8px;
}
.coupon-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
}
.coupon-scroll::-webkit-scrollbar {
  height: 6px;
}
.coupon-scroll::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}
.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
  flex: 0 0 240px;
  scroll-snap-align: start;
}
.coupon-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.coupon-left {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 16px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff;
  min-width: 90px;
}
.coupon-symbol {
  font-size: 14px;
  font-weight: 600;
  margin-right: 2px;
}
.coupon-amount {
  font-size: 32px;
  font-weight: 800;
}
.coupon-right {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}
.coupon-name {
  font-size: 15px;
  font-weight: 700;
}
.coupon-threshold {
  font-size: 12px;
  color: #999;
}
.coupon-btn {
  border: 1px solid var(--color-primary);
  background: #fff;
  color: var(--color-primary);
  padding: 4px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  width: fit-content;
  transition: all 0.2s;
}
.coupon-btn:hover {
  background: var(--color-primary);
  color: #fff;
}

/* ═══ 品牌推荐 ═══ */
.brand-section {
  margin-top: 8px;
}
.brand-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}
.brand-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.brand-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.brand-img {
  width: 64px;
  height: 64px;
  object-fit: contain;
  border-radius: 12px;
  margin-bottom: 10px;
  background: #f8f8f8;
}
.brand-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

/* ═══ 用户评价 ═══ */
.review-section {
  margin-top: 8px;
}
.review-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.review-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.review-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.review-stars {
  display: flex;
  gap: 2px;
}
.review-star {
  color: #ddd;
  font-size: 14px;
}
.review-star.active {
  color: #ffc107;
}
.review-date {
  font-size: 12px;
  color: #bbb;
}
.review-content {
  font-size: 14px;
  line-height: 1.6;
  color: #555;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.review-product {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}
.review-product-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
  border-radius: 4px;
  background: #f8f8f8;
}
.review-product span {
  font-size: 12px;
  color: #999;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ═══ Footer ═══ */
.home-footer {
  text-align: center;
  padding: 40px 20px;
  color: #bbb;
  font-size: 13px;
}

/* ═══ 响应式 ═══ */
@media (max-width: 992px) {
  .hero-row {
    flex-direction: column;
  }
  .category-sidebar {
    width: 100%;
    border-radius: 12px;
    order: -1;
    max-height: none;
  }
  .sidebar-header {
    display: none;
  }
  .l1-list {
    display: flex;
    flex-wrap: wrap;
    padding: 6px 8px;
    gap: 4px;
    overflow-y: visible;
  }
  .l1-item {
    padding: 8px 14px;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.1);
  }
  .l2-panel {
    display: none;
  }
  .banner-wrap :deep(.el-carousel__container),
  .banner-slide,
  .banner-placeholder {
    height: 240px !important;
  }
  .composite-layout {
    height: 240px;
  }
  .center-overlay {
    padding: 12px 16px 10px;
    gap: 6px;
  }
  .banner-title {
    font-size: 16px;
  }
  .badge-item {
    padding: 3px 10px;
  }
  .badge-name {
    font-size: 11px;
  }
  .badge-price {
    font-size: 12px;
  }
  .mini-coupon {
    padding: 3px 8px;
  }
  .mc-amount {
    font-size: 13px;
  }
  .header-nav {
    display: none;
  }
  .promo-card {
    width: 280px;
    flex: 0 0 280px;
  }
}

@media (max-width: 768px) {
  .search-top {
    flex-wrap: wrap;
    gap: 12px;
  }
  .brand-main {
    font-size: 28px;
  }
  .brand-sub {
    font-size: 14px;
  }
  .search-area {
    order: 3;
    max-width: none;
    width: 100%;
  }
  .composite-layout {
    flex-direction: column;
  }
  .side-product {
    flex-direction: row;
    flex: none;
    height: 80px;
  }
  .side-product img {
    width: 80px;
    height: 80px;
    flex: none;
  }
  .center-product {
    flex: 1;
  }
  .product-card {
    width: calc(50% - 8px);
    flex: 0 0 calc(50% - 8px);
  }
  .brand-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .review-grid {
    grid-template-columns: 1fr;
  }
  .promo-card {
    width: 260px;
    flex: 0 0 260px;
  }
  .coupon-card {
    flex: 0 0 200px;
  }
  .seckill-entry {
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px 14px;
  }
  .seckill-tag {
    font-size: 16px;
  }
  .promo-badges {
    gap: 6px;
  }
  .banner-coupons {
    gap: 4px;
  }
}

@media (max-width: 640px) {
  .home-body {
    padding: 12px 12px 0;
  }
  .product-card {
    width: calc(50% - 8px);
    flex: 0 0 calc(50% - 8px);
  }
  .search-header {
    padding: 16px 0 12px;
  }
  .search-box {
    height: 42px;
  }
  .search-btn {
    padding: 0 20px;
    font-size: 14px;
  }
  .hot-keywords {
    font-size: 12px;
  }
}
</style>
