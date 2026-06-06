<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchHomeData, trackSearch } from '@/api/notice'
import { type Product } from '@/api/product'
import { fetchCategories, type Category } from '@/api/product'
import { imageOrPlaceholder } from '@/utils/image'

const router = useRouter()
const searchKeyword = ref('')

// ═══ 首页数据 ═══
const banners = ref<{ id: number; imageUrl: string; linkUrl: string; title?: string }[]>([])
const hotProducts = ref<Product[]>([])
const newProducts = ref<Product[]>([])
const hotSearches = ref<string[]>([])
const categories = ref<Category[]>([])

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
    categories.value = catRes
  } catch { /* 静默降级 */ }
}

function goSearch() {
  const kw = searchKeyword.value.trim()
  if (kw) {
    trackSearch(kw)
    router.push({ path: '/search', query: { keyword: kw } })
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

onMounted(loadHome)
</script>

<template>
  <div class="home-root">

    <!-- ═══ 搜索区 ═══ -->
    <section class="hero-section">
      <div class="hero-inner">
        <h1 class="hero-title">发现你的下一件好物</h1>
        <p class="hero-subtitle">精选全球好物，品质生活从这里开始</p>
        <div class="hero-search">
          <input
            v-model="searchKeyword"
            placeholder="搜索你想要的商品..."
            class="hero-search-input"
            @keydown.enter="goSearch"
          />
          <button class="hero-search-btn" @click="goSearch">搜索</button>
        </div>
        <div v-if="hotSearches.length" class="hot-tags">
          <span class="hot-tags-label">热门搜索：</span>
          <button v-for="kw in hotSearches.slice(0, 8)" :key="kw" class="hot-tag" @click="goSearchTag(kw)">
            {{ kw }}
          </button>
        </div>
      </div>
    </section>

    <!-- ═══ 分类导航 ═══ -->
    <section v-if="categories.length" class="category-section">
      <div class="page-container">
        <div class="section-title">
          <h2>商品分类</h2>
        </div>
        <div class="category-grid">
          <div
            v-for="cat in categories.filter(c => !c.parentId).slice(0, 8)"
            :key="cat.id"
            class="category-card"
            @click="goCategory(cat.id)"
          >
            <span class="category-name">{{ cat.name }}</span>
            <div v-if="cat.children" class="category-children">
              <span v-for="sub in cat.children.slice(0, 4)" :key="sub.id" class="category-child">
                {{ sub.name }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ═══ 轮播 ═══ -->
    <section v-if="banners.length" class="banner-section page-container">
      <el-carousel :interval="4000" type="card" height="320px" indicator-position="outside">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <img :src="b.imageUrl" :alt="b.title || ''" class="banner-img" @click="goSearch" />
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- ═══ 热门商品 ═══ -->
    <section v-if="hotProducts.length" class="product-section">
      <div class="page-container">
        <div class="section-title">
          <h2>热门推荐</h2>
          <router-link to="/products?sort=sales_desc">查看更多 →</router-link>
        </div>
        <div class="product-grid">
          <div v-for="p in hotProducts.slice(0, 8)" :key="p.id" class="product-card" @click="goProduct(p.id)">
            <div class="product-img-box">
              <img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.name" loading="lazy" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ p.name }}</h3>
              <div class="product-price-row">
                <span class="product-price">¥{{ p.price }}</span>
                <span class="product-sales">已售 {{ p.sales }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ═══ 新品推荐 ═══ -->
    <section v-if="newProducts.length" class="product-section">
      <div class="page-container">
        <div class="section-title">
          <h2>新品上架</h2>
          <router-link to="/products?sort=newest">查看更多 →</router-link>
        </div>
        <div class="product-grid">
          <div v-for="p in newProducts.slice(0, 8)" :key="p.id" class="product-card" @click="goProduct(p.id)">
            <div class="product-img-box">
              <img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.name" loading="lazy" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ p.name }}</h3>
              <div class="product-price-row">
                <span class="product-price">¥{{ p.price }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ═══ 底部 ═══ -->
    <footer class="home-footer">
      <p>© 2026 优品商城 · 课程演示项目</p>
    </footer>
  </div>
</template>

<style scoped>
.home-root { overflow: hidden; }

/* ═══ Hero ═══ */
.hero-section {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c5a 50%, #ffa366 100%);
  padding: 60px 20px 48px;
  text-align: center;
}
.hero-inner { max-width: 720px; margin: 0 auto; }
.hero-title { font-size: 36px; font-weight: 800; color: #fff; margin-bottom: 8px; }
.hero-subtitle { font-size: 16px; color: rgba(255,255,255,0.85); margin-bottom: 28px; }
.hero-search { display: flex; max-width: 520px; margin: 0 auto 20px; }
.hero-search-input {
  flex: 1; height: 48px; padding: 0 20px; border: none; border-radius: 24px 0 0 24px;
  font-size: 15px; outline: none;
}
.hero-search-btn {
  height: 48px; padding: 0 28px; border: none; border-radius: 0 24px 24px 0;
  background: #1a1a1a; color: #fff; font-size: 15px; font-weight: 600; cursor: pointer;
  transition: background 0.2s;
}
.hero-search-btn:hover { background: #333; }

.hot-tags { display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 8px; }
.hot-tags-label { color: rgba(255,255,255,0.7); font-size: 13px; }
.hot-tag {
  padding: 3px 14px; border: 1px solid rgba(255,255,255,0.35); border-radius: 14px;
  background: transparent; color: rgba(255,255,255,0.9); font-size: 13px; cursor: pointer; transition: all 0.2s;
}
.hot-tag:hover { background: rgba(255,255,255,0.15); border-color: rgba(255,255,255,0.6); }

/* ═══ 分类 ═══ */
.category-section { padding: 20px 0 0; }
.category-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.category-card {
  padding: 20px; border-radius: 12px; background: #fff; cursor: pointer;
  transition: all 0.2s; box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.category-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); transform: translateY(-2px); }
.category-name { font-size: 16px; font-weight: 600; display: block; margin-bottom: 8px; }
.category-children { display: flex; flex-wrap: wrap; gap: 6px; }
.category-child { font-size: 12px; color: #888; background: #f5f5f5; padding: 2px 8px; border-radius: 4px; }

/* ═══ 轮播 ═══ */
.banner-section { margin-top: 32px; }
.banner-img { width: 100%; height: 100%; object-fit: cover; border-radius: 12px; cursor: pointer; }

/* ═══ 商品 ═══ */
.product-section { padding: 20px 0 40px; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card {
  background: #fff; border-radius: 12px; overflow: hidden; cursor: pointer;
  transition: all 0.2s; box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.product-card:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.1); transform: translateY(-3px); }
.product-img-box {
  width: 100%; aspect-ratio: 1; background: #f8f8f8;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.product-img-box img { max-width: 100%; max-height: 100%; object-fit: contain; transition: transform 0.3s; }
.product-card:hover .product-img-box img { transform: scale(1.05); }
.product-info { padding: 12px; }
.product-name {
  font-size: 14px; font-weight: 500; margin-bottom: 8px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.product-price-row { display: flex; align-items: center; justify-content: space-between; }
.product-price { font-size: 18px; font-weight: 700; color: var(--color-price, #ff0036); }
.product-sales { font-size: 12px; color: #aaa; }

/* ═══ Footer ═══ */
.home-footer { text-align: center; padding: 40px 20px; color: #bbb; font-size: 13px; }

/* ═══ 响应式 ═══ */
@media (max-width: 1024px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
  .category-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .hero-title { font-size: 24px; }
  .hero-subtitle { font-size: 14px; }
  .hero-section { padding: 40px 16px 32px; }
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .category-grid { grid-template-columns: repeat(2, 1fr); gap: 8px; }
  .banner-section { margin-top: 20px; }
}
@media (max-width: 390px) {
  .hero-section { padding: 32px 12px 24px; }
  .hero-title { font-size: 20px; }
  .hero-search-input { height: 42px; padding: 0 14px; }
  .hero-search-btn { height: 42px; padding: 0 20px; font-size: 14px; }
}
</style>
