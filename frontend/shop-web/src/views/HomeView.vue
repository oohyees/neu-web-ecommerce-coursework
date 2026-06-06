<template>
  <ShopLayout>
    <div class="page-wrap">
      <!-- ===== Hero: 三栏布局 ===== -->
      <section class="home-hero">
        <!-- 左侧分类面板 (毛玻璃效果 — 董耕赫风格) -->
        <aside class="category-panel glass-panel">
          <h2 class="category-title">全部分类</h2>
          <div v-if="!categoriesLoaded" class="loading-skeleton">
            <div v-for="n in 6" :key="n" class="skeleton-line" />
          </div>
          <div v-for="group in categoryGroups" :key="group.id" class="category-group">
            <button class="parent-category" @click="$router.push(`/products?categoryId=${group.id}`)">
              <strong>{{ group.name }}</strong>
              <span>{{ group.children.map(c => c.name).slice(0, 3).join(' / ') || '精选好物' }}</span>
            </button>
            <div v-if="group.children.length" class="child-categories">
              <button v-for="child in group.children.slice(0, 4)" :key="child.id" @click="$router.push(`/products?categoryId=${child.id}`)">
                {{ child.name }}
              </button>
            </div>
          </div>
          <p v-if="categoriesLoaded && !categoryGroups.length" class="empty-tip">暂无分类，请在后台维护分类数据。</p>
        </aside>

        <!-- 中间 Banner 区 (杂志风大标题 — 董耕赫风格) -->
        <div class="banner-area">
          <el-carousel v-if="data.banners.length" class="hero-carousel" height="360px" trigger="click">
            <el-carousel-item v-for="b in data.banners" :key="b.id">
              <div class="banner-slide" :style="{ backgroundColor: b.bgColor || '#1f2933' }">
                <img :src="b.imageUrl" :alt="b.title" />
                <div class="banner-copy">
                  <strong>{{ b.title || '商城精选活动' }}</strong>
                  <el-button type="danger" size="small" round @click="$router.push(b.linkUrl || '/products')">立即查看</el-button>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <!-- 无Banner时的渐变 Hero (董耕赫风格大标题) -->
          <section v-else class="banner-gradient">
            <div class="banner-gradient-inner">
              <p class="hero-eyebrow">JINGXUAN MALL</p>
              <h1 class="hero-headline">今天想买的<br/>都在精选</h1>
              <p class="hero-lede">
                精选好物、限时优惠和售后客服都已就位，登录后即可下单。
              </p>
              <div class="hero-actions">
                <RouterLink to="/products" class="hero-btn-primary">浏览商品</RouterLink>
                <RouterLink to="/cart" class="hero-btn-secondary">查看购物车</RouterLink>
              </div>
            </div>
          </section>
        </div>

        <!-- 右侧用户面板 -->
        <aside class="service-panel">
          <!-- 用户问候 -->
          <div class="user-greeting">
            <div class="user-avatar">{{ (userStore.nickname || '嗨')[0] }}</div>
            <div>
              <strong>{{ userStore.nickname || '你好，欢迎选购' }}</strong>
              <p v-if="!userStore.userId">登录后可享更多专属优惠</p>
              <p v-else class="muted">{{ userStore.email || '' }}</p>
            </div>
          </div>
          <div class="quick-actions">
            <el-button type="danger" @click="$router.push(userStore.userId ? '/user/orders' : '/login')">我的订单</el-button>
            <el-button @click="$router.push('/cart')">购物车</el-button>
          </div>

          <el-divider />

          <!-- 商城公告 -->
          <strong class="panel-head">📢 商城公告</strong>
          <p v-if="!announcements.length && !notices.length" class="empty-tip muted">暂无公告</p>
          <div v-else class="notice-list">
            <p v-for="a in announcements.slice(0, 3)" :key="a.id" class="notice-line" @click="$router.push('/notices')">
              <span class="notice-dot" />{{ a.title }}
            </p>
            <p v-for="n in notices.slice(0, 3)" :key="`n-${n.id}`" class="notice-line" @click="$router.push('/notices')">
              <span class="notice-dot notice-dot--activity" />{{ n.title }}
            </p>
          </div>

          <!-- 热门搜索 -->
          <strong class="panel-head" style="margin-top:14px">🔥 热门搜索</strong>
          <div class="hot-tags">
            <button v-for="(word, i) in hotSearchWords.slice(0, 6)" :key="i" @click="goSearch(word)">{{ word }}</button>
          </div>
        </aside>
      </section>

      <!-- ===== 搜索面板 ===== -->
      <section class="search-panel glass-panel">
        <el-input
          v-model="searchKeyword"
          size="large"
          :placeholder="currentPlaceholder"
          @keyup.enter="goSearch(searchKeyword)"
        >
          <template #append>
            <el-button type="danger" @click="goSearch(searchKeyword)">搜索</el-button>
          </template>
        </el-input>
        <div class="hot-search">
          <span>热门搜索</span>
          <button v-for="word in hotSearchWords.slice(0, 6)" :key="word" @click="goSearch(word)">{{ word }}</button>
        </div>
      </section>

      <!-- ===== 系统公告滚动条 (李旋风格) ===== -->
      <div v-if="notices.length" class="notice-bar" @click="$router.push('/notices')">
        <span class="notice-tag">系统公告</span>
        <el-carousel height="24px" direction="vertical" :autoplay="true" indicator-position="none" :interval="4000">
          <el-carousel-item v-for="n in notices.slice(0, 5)" :key="n.id">
            <span class="notice-text">{{ n.title }} · {{ n.content?.slice(0, 40) || '' }}</span>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- ===== 秒杀入口 (李旋风格渐变红底) ===== -->
      <div v-if="promotions.length" class="seckill-entry" @click="$router.push('/products?sort=sales_desc')">
        <span class="seckill-tag">⚡ 限时秒杀</span>
        <span class="seckill-desc">{{ promotions[0]?.title || '爆款直降 · 抢完即止' }}</span>
        <span class="seckill-go">立即抢购 →</span>
      </div>

      <!-- ===== 促销/优惠券四宫格 (你的设计) ===== -->
      <section v-if="promotions.length || coupons.length" class="promo-grid">
        <article v-for="p in promotions.slice(0, 2)" :key="`p-${p.id}`" class="promo-card" @click="$router.push('/products')">
          <span class="promo-tag">促销</span>
          <strong>{{ p.title }}</strong>
          <p>{{ p.promotionType || '限时特惠' }}</p>
          <el-button link type="danger">立即抢购 →</el-button>
        </article>
        <article v-for="c in coupons.slice(0, Math.max(0, 4 - Math.min(promotions.length, 2)))" :key="`c-${c.id}`" class="promo-card coupon-card" @click="claimCoupon(c.id)">
          <span class="promo-tag coupon">优惠券</span>
          <strong>{{ c.name }}</strong>
          <p>满 ¥{{ c.thresholdAmount }} 减 ¥{{ c.discountAmount }}</p>
          <el-button link type="danger">立即领取 →</el-button>
        </article>
      </section>

      <!-- ===== 热门商品 横滚区 (李旋风格) ===== -->
      <div class="section-title">
        <h2>🔥 热门商品</h2>
        <el-button link type="danger" @click="$router.push('/products?sort=sales_desc')">查看更多 →</el-button>
      </div>
      <section v-if="data.hotProducts.length" class="product-scroll">
        <ProductCard
          v-for="p in data.hotProducts.slice(0, 8)"
          :key="p.id"
          :product="p"
          badge="热卖"
          @add="addToCart"
          @favorite="favorite"
        />
      </section>
      <EmptyState v-else title="暂无热门商品" description="请先在后台维护商品数据。" />

      <!-- ===== 新品推荐 ===== -->
      <div class="section-title">
        <h2>✨ 新品首发</h2>
        <el-button link type="danger" @click="$router.push('/products?sort=newest')">查看更多 →</el-button>
      </div>
      <section v-if="data.newProducts.length" class="product-scroll">
        <ProductCard
          v-for="p in data.newProducts.slice(0, 8)"
          :key="p.id"
          :product="p"
          badge="新品"
          @add="addToCart"
          @favorite="favorite"
        />
      </section>
      <EmptyState v-else-if="data.hotProducts.length" title="暂无新品" description="敬请期待更多新品上架。" />

      <!-- ===== 促销商品 ===== -->
      <template v-if="data.promotionProducts?.length">
        <div class="section-title">
          <h2>🎉 促销专区</h2>
          <el-button link type="danger" @click="$router.push('/products?sort=price_asc')">查看更多 →</el-button>
        </div>
        <section class="product-scroll">
          <ProductCard
            v-for="p in data.promotionProducts.slice(0, 8)"
            :key="p.id"
            :product="p"
            badge="促销"
            @add="addToCart"
            @favorite="favorite"
          />
        </section>
      </template>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useUserStore } from '@/stores/user'
import type { HomeData, Category, Announcement, ActivityNotice, Promotion, Coupon } from '@/types'
import ShopLayout from '@/layouts/ShopLayout.vue'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()
const router = useRouter()
const data = ref<HomeData>({
  banners: [], hotProducts: [], newProducts: [], promotionProducts: [],
  hotSearches: [], categories: [], announcements: [], activities: [], hotKeywords: [],
})
const announcements = ref<Announcement[]>([])
const notices = ref<ActivityNotice[]>([])
const promotions = ref<Promotion[]>([])
const categories = ref<Category[]>([])
const coupons = ref<Coupon[]>([])
const categoriesLoaded = ref(false)
const searchKeyword = ref('')
const hotSearchWords = ref<string[]>(['键盘', '鼠标', 'SSD', '耳机', '手机', '笔记本'])
const placeholderIndex = ref(0)
let placeholderTimer: ReturnType<typeof setInterval> | null = null

const rotatingPlaceholders = computed(() => {
  const fromHot = hotSearchWords.value
  const fromProducts = (data.value.hotProducts || []).slice(0, 6).map((p) => p.name)
  return [...fromHot, ...fromProducts].filter(Boolean)
})

const currentPlaceholder = computed(() => {
  const list = rotatingPlaceholders.value
  if (!list.length) return '搜索精选好物'
  return list[placeholderIndex.value % list.length]
})

const categoryGroups = computed(() =>
  (categories.value || [])
    .filter((c: Category) => !c.parentId)
    .map((parent: Category) => ({
      ...parent,
      children: (categories.value || []).filter((c: Category) => c.parentId === parent.id),
    }))
)

function goSearch(word: string) {
  const keyword = (typeof word === 'string' ? word : searchKeyword.value).trim()
  if (!keyword) return router.push('/products')
  api.post('/home/search/track', { keyword }).catch(() => {})
  router.push(`/products?keyword=${encodeURIComponent(keyword)}&searchMode=fuzzy`)
}

async function addToCart(product: any) {
  if (!userStore.userId) return ElMessage.warning('请先登录后加入购物车')
  await api.post('/cart/items', { userId: userStore.userId, productId: product.id, quantity: 1 })
  ElMessage.success('已加入购物车')
}

async function favorite(product: any) {
  if (!userStore.userId) return ElMessage.warning('请先登录后收藏')
  await api.post(`/favorites/${product.id}`, null, { params: { userId: userStore.userId } })
  ElMessage.success('已收藏商品')
}

async function claimCoupon(couponId: number) {
  if (!userStore.userId) return ElMessage.warning('请先登录后领取优惠券')
  await api.post(`/marketing/coupons/${couponId}/claim`, null, { params: { userId: userStore.userId } })
  ElMessage.success('优惠券已领取')
}

onMounted(async () => {
  try {
    const [homeRes, announcementRes, noticeRes, promotionRes, categoryRes, couponRes] = await Promise.all([
      api.get('/home'),
      api.get('/announcements'),
      api.get('/activity-notices'),
      api.get('/marketing/promotions'),
      api.get('/categories'),
      api.get('/marketing/coupons'),
    ])
    data.value = { ...data.value, ...(homeRes.data.data || homeRes.data) }
    if (data.value.hotSearches) hotSearchWords.value = data.value.hotSearches.map((h: any) => h.keyword)
    announcements.value = announcementRes.data.data || []
    notices.value = noticeRes.data.data || []
    promotions.value = promotionRes.data.data || []
    categories.value = categoryRes.data.data || []
    categoriesLoaded.value = true
    coupons.value = couponRes.data.data || []
  } catch {
    categoriesLoaded.value = true
  }

  placeholderTimer = setInterval(() => {
    const len = rotatingPlaceholders.value.length
    if (len) placeholderIndex.value = (placeholderIndex.value + 1) % len
  }, 2800)
})

onUnmounted(() => {
  if (placeholderTimer) clearInterval(placeholderTimer)
})
</script>

<style scoped>
/* ===== Hero 三栏 ===== */
.home-hero {
  display: grid;
  grid-template-columns: 228px minmax(0, 1fr) 248px;
  gap: 16px;
  margin-bottom: 20px;
}

/* ===== 左侧分类面板 (毛玻璃) ===== */
.category-panel {
  padding: 16px;
  overflow-y: auto;
  max-height: 540px;
}
.category-title {
  margin: 0 0 14px;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 700;
}
.loading-skeleton { display: grid; gap: 10px; padding: 8px 0; }
.skeleton-line {
  height: 36px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  border-radius: var(--radius-sm);
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
.category-group {
  padding: 8px 0;
  border-bottom: 1px solid var(--line-soft);
}
.category-group:last-child { border-bottom: none; }
.parent-category {
  display: grid;
  gap: 3px;
  width: 100%;
  padding: 4px 0;
  text-align: left;
  cursor: pointer;
  background: none;
  border: 0;
  transition: color var(--duration-fast) var(--ease-out);
}
.parent-category:hover strong { color: var(--brand); }
.parent-category strong { font-size: 14px; font-weight: 600; transition: color var(--duration-fast) var(--ease-out); }
.parent-category span { color: var(--muted); font-size: 12px; }

.child-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}
.child-categories button {
  padding: 3px 8px;
  cursor: pointer;
  color: var(--muted);
  background: var(--panel-alt);
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  font-size: 12px;
  transition: all var(--duration-fast) var(--ease-out);
}
.child-categories button:hover {
  color: var(--brand);
  border-color: #f2b8c2;
  background: var(--brand-light);
}

/* ===== Banner 区 ===== */
.banner-area {
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.hero-carousel {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: linear-gradient(135deg, #fff7ed 0%, #fff1f2 45%, #eef2ff 100%);
}
.hero-carousel :deep(.el-carousel__button) {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.banner-slide {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
.banner-slide img {
  width: 100%;
  height: 100%;
  padding: 20px;
  object-fit: contain;
  background: transparent;
}
.banner-copy {
  position: absolute;
  right: 24px;
  bottom: 24px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 18px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-md);
}
.banner-copy strong {
  font-size: 15px;
  font-weight: 700;
}

/* 渐变 Hero (董耕赫风格大标题) */
.banner-gradient {
  height: 360px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: linear-gradient(135deg, #ff6b6b 0%, #e60023 30%, #cc0020 65%, #ff4757 100%);
}
.banner-gradient-inner {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  height: 100%;
  padding: 48px;
  color: #fff;
}
.hero-eyebrow {
  margin: 0 0 12px;
  font-size: 0.72rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  opacity: 0.85;
}
.hero-headline {
  margin: 0 0 16px;
  font-family: var(--font-display);
  font-size: clamp(2.6rem, 5vw, 4.2rem);
  font-weight: 800;
  line-height: 1.05;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
}
.hero-lede {
  max-width: 32rem;
  margin: 0 0 24px;
  font-size: 1.05rem;
  line-height: 1.7;
  opacity: 0.9;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.hero-btn-primary,
.hero-btn-secondary {
  padding: 12px 24px;
  border-radius: var(--radius-full);
  font-weight: 700;
  font-size: 15px;
  transition: all var(--duration-fast) var(--ease-out);
}
.hero-btn-primary {
  color: var(--brand);
  background: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}
.hero-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.16);
}
.hero-btn-secondary {
  color: #fff;
  border: 1.5px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}
.hero-btn-secondary:hover {
  background: rgba(255, 255, 255, 0.22);
  transform: translateY(-2px);
}

/* ===== 右侧用户面板 ===== */
.service-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
.user-greeting {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-avatar {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  color: #fff;
  background: linear-gradient(135deg, var(--brand), var(--brand-dark));
  border-radius: 50%;
  font-size: 17px;
  font-weight: 700;
}
.user-greeting strong { font-size: 15px; }
.user-greeting p { margin: 2px 0 0; font-size: 13px; }

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.panel-head {
  display: block;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 8px;
}
.notice-list {
  display: grid;
  gap: 6px;
}
.notice-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 13px;
  cursor: pointer;
  transition: color var(--duration-fast) var(--ease-out);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-line:hover { color: var(--brand); }
.notice-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  flex-shrink: 0;
  background: var(--brand);
  border-radius: 50%;
}
.notice-dot--activity { background: var(--warning); }

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.hot-tags button {
  padding: 4px 10px;
  cursor: pointer;
  color: var(--muted);
  background: var(--panel-alt);
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  font-size: 12px;
  transition: all var(--duration-fast) var(--ease-out);
}
.hot-tags button:hover {
  color: var(--brand);
  border-color: #f2b8c2;
  background: var(--brand-light);
}

/* ===== 搜索面板 (毛玻璃) ===== */
.search-panel {
  display: grid;
  gap: 10px;
  padding: 16px;
  margin-bottom: 18px;
}
.search-panel :deep(.el-input__wrapper) {
  border-radius: var(--radius-full) 0 0 var(--radius-full);
  box-shadow: 0 0 0 1px var(--line) inset;
}
.search-panel :deep(.el-input-group__append) {
  overflow: hidden;
  background: var(--brand);
  border-color: var(--brand);
  border-radius: 0 var(--radius-full) var(--radius-full) 0;
}
.search-panel :deep(.el-input-group__append .el-button) {
  padding: 0 22px;
  color: #fff;
  background: var(--brand);
  border-color: var(--brand);
  font-weight: 600;
}
.hot-search {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.hot-search span {
  color: var(--muted-light);
  font-size: 13px;
}
.hot-search button {
  padding: 4px 10px;
  cursor: pointer;
  color: var(--ink-soft);
  background: var(--panel-alt);
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  font-size: 13px;
  transition: all var(--duration-fast) var(--ease-out);
}
.hot-search button:hover {
  color: var(--brand);
  border-color: #f2b8c2;
  background: var(--brand-light);
}

/* ===== 公告滚动条 (李旋风格) ===== */
.notice-bar {
  margin: 0 0 16px;
  padding: 10px 16px;
  background: var(--panel);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  box-shadow: var(--shadow-xs);
  cursor: pointer;
  transition: background var(--duration-fast) var(--ease-out);
  border: 1px solid var(--line-soft);
}
.notice-bar:hover { background: #fffaf7; }
.notice-tag {
  background: linear-gradient(135deg, var(--brand), #ff6b6b);
  color: #fff;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: var(--radius-xs);
  flex-shrink: 0;
  font-weight: 600;
}
.notice-text { color: var(--muted); }
.notice-bar :deep(.el-carousel) { flex: 1; min-width: 0; }

/* ===== 秒杀入口 (李旋风格) ===== */
.seckill-entry {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  margin: 0 0 16px;
  background: linear-gradient(90deg, #ff0036, #ff5000, #ff6a00);
  border-radius: var(--radius);
  color: #fff;
  cursor: pointer;
  transition: filter var(--duration-fast);
  box-shadow: 0 4px 18px rgba(255, 0, 54, 0.3);
}
.seckill-entry:hover { filter: brightness(1.05); }
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
  flex-shrink: 0;
}

/* ===== 促销/优惠券四宫格 ===== */
.promo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 24px;
}
.promo-card {
  min-height: 130px;
  padding: 18px;
  background: linear-gradient(135deg, #fff5f6, #fff);
  border: 1px solid #fecdd3;
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.promo-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}
.promo-card.coupon-card {
  background: linear-gradient(135deg, #fffaf0, #fff);
  border-color: #ffe0b2;
}
.promo-tag {
  display: inline-block;
  width: fit-content;
  padding: 3px 10px;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  background: var(--brand);
  border-radius: var(--radius-full);
  margin-bottom: 10px;
}
.promo-tag.coupon { background: #ff7a00; }
.promo-card strong { font-size: 16px; margin-bottom: 6px; }
.promo-card p { color: var(--muted); font-size: 13px; margin: 0 0 8px; }
.promo-card :deep(.el-button) { margin-top: auto; align-self: flex-start; padding: 0; font-weight: 600; }

/* ===== 商品横滚区 ===== */
.product-scroll {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .home-hero {
    grid-template-columns: 1fr;
  }
  .category-panel { max-height: none; }
  .promo-grid { grid-template-columns: repeat(2, 1fr); }
  .product-scroll { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .home-hero { grid-template-columns: 1fr; }
  .banner-area { order: -1; }
  .banner-gradient { height: 260px; }
  .banner-gradient-inner { padding: 28px; }
  .hero-headline { font-size: clamp(1.8rem, 6vw, 2.8rem); }
  .hero-lede { font-size: 0.9rem; line-height: 1.5; }
  .hero-actions { width: 100%; }
  .hero-btn-primary, .hero-btn-secondary { width: 100%; text-align: center; }
  .promo-grid { grid-template-columns: repeat(2, 1fr); }
  .product-scroll { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .seckill-entry { flex-wrap: wrap; gap: 10px; padding: 12px 14px; }
  .seckill-tag { font-size: 16px; }
  .notice-bar { font-size: 12px; padding: 8px 12px; }
  .banner-copy {
    right: 10px; bottom: 10px; left: 10px;
    justify-content: space-between;
    padding: 8px 12px;
  }
  .banner-copy strong { font-size: 13px; }
  .hero-carousel :deep(.el-carousel__container) { height: 260px !important; }
}
@media (max-width: 540px) {
  .promo-grid { grid-template-columns: 1fr; }
  .product-scroll { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
  .category-panel { max-height: none; }
}
</style>
