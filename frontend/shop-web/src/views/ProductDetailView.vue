<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useFavoriteStore } from '@/stores/favorite'
import {
  fetchProductDetail,
  fetchReviews,
  submitReview,
  type Product,
  type ProductSku,
  type ProductImage,
  type ProductSpec,
  type Review,
} from '@/api/product'
import { addToCart } from '@/api/cart'
import { imageOrPlaceholder } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const favoriteStore = useFavoriteStore()

// ═══ 商品数据 ═══
const product = ref<Product | null>(null)
const skus = ref<ProductSku[]>([])
const images = ref<ProductImage[]>([])
const specs = ref<ProductSpec[]>([])
const reviews = ref<Review[]>([])
const loading = ref(true)

// ═══ SKU 选择 ═══
const selectedColor = ref<string | null>(null)
const selectedSize = ref<string | null>(null)
const quantity = ref(1)

// 从SKU列表提取可选的color和size
const colorOptions = computed(() => {
  const set = new Set<string>()
  skus.value.forEach(s => { if (s.color) set.add(s.color) })
  return Array.from(set)
})

const sizeOptions = computed(() => {
  const set = new Set<string>()
  skus.value.forEach(s => { if (s.size) set.add(s.size) })
  return Array.from(set)
})

// 当前选中SKU
const currentSku = computed(() => {
  return skus.value.find(s => {
    const colorMatch = !colorOptions.value.length || s.color === selectedColor.value
    const sizeMatch = !sizeOptions.value.length || s.size === selectedSize.value
    return colorMatch && sizeMatch
  }) || null
})

// 动态价格和库存
const displayPrice = computed(() => currentSku.value?.price ?? product.value?.price ?? 0)
const displayStock = computed(() => currentSku.value?.stock ?? product.value?.stock ?? 0)
const displayOriginalPrice = computed(() => product.value?.originalPrice ?? null)
const specValue = computed(() => {
  const map = new Map(specs.value.map((s) => [s.specName, s.specValue]))
  if (product.value?.paramsText) {
    product.value.paramsText.split(';').forEach((part) => {
      const [name, ...valueParts] = part.split(':')
      if (name && valueParts.length && !map.has(name.trim())) map.set(name.trim(), valueParts.join(':').trim())
    })
  }
  return (name: string) => map.get(name) || ''
})

// 图片列表
const mainImage = computed(() => imageOrPlaceholder(images.value[0]?.url || product.value?.imageUrl))
const thumbImages = computed(() => images.value.length > 1 ? images.value.map(i => i.url) : [mainImage.value])
const activeImage = ref('')
watch(mainImage, (url) => { activeImage.value = url })

// 规格文本（加购用）
const specText = computed(() => {
  if (!currentSku.value) return null
  const parts = []
  if (currentSku.value.color) parts.push(currentSku.value.color)
  if (currentSku.value.size) parts.push(currentSku.value.size)
  return parts.length ? parts.join(' / ') : null
})

// 是否收藏
const isFav = computed(() => product.value ? favoriteStore.isFavorite(product.value.id) : false)

// ═══ 评分 ═══
const reviewForm = ref({ rating: 5, content: '' })
const submittingReview = ref(false)

// ═══ 方法 ═══
async function loadProduct() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const detail = await fetchProductDetail(id)
    product.value = detail.product
    skus.value = detail.skus || []
    images.value = detail.images || []
    specs.value = detail.specs || []

    // 默认选中第一个SKU的颜色和尺寸
    if (skus.value.length > 0) {
      if (colorOptions.value.length) selectedColor.value = skus.value[0].color
      if (sizeOptions.value.length) selectedSize.value = skus.value[0].size
    }

    // 加载评价
    const revData: any = await fetchReviews(id)
    reviews.value = revData?.items ?? (Array.isArray(revData) ? revData : [])
  } catch {
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

function selectColor(color: string) { selectedColor.value = color }
function selectSize(size: string) { selectedSize.value = size }

function handleAddToCart() {
  if (!product.value) return
  addToCart({
    productId: product.value.id,
    skuId: currentSku.value?.id,
    specText: specText.value ?? undefined,
    quantity: quantity.value,
  }).then(() => {
    ElMessage.success('已添加到购物车')
    cartStore.refresh()
  }).catch(() => {})
}

async function handleBuyNow() {
  if (!product.value) return
  try {
    await addToCart({
      productId: product.value.id,
      skuId: currentSku.value?.id,
      specText: specText.value ?? undefined,
      quantity: quantity.value,
    })
    ElMessage.success('已添加到购物车')
    cartStore.refresh()
    router.push('/cart')
  } catch { /* handled by interceptor */ }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!product.value) return
  try {
    const result = await favoriteStore.toggle(product.value.id)
    ElMessage.success(result ? '已收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleSubmitReview() {
  if (!product.value || !reviewForm.value.content.trim()) return
  submittingReview.value = true
  try {
    await submitReview({
      productId: product.value.id,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
    })
    ElMessage.success('评价提交成功')
    reviewForm.value.content = ''
    loadProduct()
  } catch { /* handled by interceptor */ }
  finally { submittingReview.value = false }
}

onMounted(loadProduct)
watch(() => route.params.id, loadProduct)
</script>

<template>
  <div v-if="loading" class="detail-loading">
    <el-skeleton :rows="10" animated />
  </div>

  <div v-else-if="product" class="detail-root">
    <!-- ═══ 商品主体 ═══ -->
    <div class="detail-main">
      <!-- 图片区 -->
      <div class="detail-gallery">
        <div class="gallery-main">
          <img :src="activeImage" :alt="product.name" />
        </div>
        <div v-if="thumbImages.length > 1" class="gallery-thumbs">
          <img
            v-for="(url, i) in thumbImages"
            :key="i"
            :src="url"
            :class="{ active: activeImage === url }"
            @click="activeImage = url"
          />
        </div>
      </div>

      <!-- 信息区 -->
      <div class="detail-info">
        <h1 class="info-name">{{ product.name }}</h1>
        <p v-if="product.subtitle" class="info-subtitle">{{ product.subtitle }}</p>

        <!-- 价格 -->
        <div class="info-price">
          <span class="price-current">¥{{ displayPrice }}</span>
          <span v-if="displayOriginalPrice && displayOriginalPrice > displayPrice" class="price-original">
            ¥{{ displayOriginalPrice }}
          </span>
        </div>
        <div class="dummy-meta">
          <span>{{ product.brand || specValue('品牌') || 'DummyJSON' }}</span>
          <span v-if="product.rating || specValue('评分')">评分 {{ product.rating || specValue('评分') }}</span>
          <span v-if="product.discountPercentage || specValue('折扣')">折扣 {{ product.discountPercentage || specValue('折扣') }}</span>
          <span v-if="product.sku || specValue('SKU')">SKU {{ product.sku || specValue('SKU') }}</span>
        </div>

        <!-- SKU 选择器 -->
        <div v-if="colorOptions.length" class="sku-group">
          <span class="sku-label">{{ colorOptions.length > 1 ? '颜色' : '规格' }}</span>
          <div class="sku-options">
            <button
              v-for="c in colorOptions"
              :key="c"
              :class="{ active: selectedColor === c }"
              @click="selectColor(c)"
            >{{ c }}</button>
          </div>
        </div>

        <div v-if="sizeOptions.length" class="sku-group">
          <span class="sku-label">尺寸</span>
          <div class="sku-options">
            <button
              v-for="s in sizeOptions"
              :key="s"
              :class="{ active: selectedSize === s }"
              :disabled="!skus.some(sk => sk.color === selectedColor && sk.size === s)"
              @click="selectSize(s)"
            >{{ s }}</button>
          </div>
        </div>

        <!-- 库存 -->
        <div class="info-stock">
          <span>库存：</span>
          <span :class="displayStock > 0 ? 'in-stock' : 'out-stock'">
            {{ displayStock > 0 ? `${displayStock} 件` : '暂时缺货' }}
          </span>
        </div>
        <div class="detail-assurance">
          <span>本地图片兜底</span>
          <span>规格库存联动</span>
          <span>评价可追溯</span>
        </div>

        <!-- 数量 + 操作 -->
        <div class="info-actions">
          <el-input-number v-model="quantity" :min="1" :max="Math.min(displayStock, 99)" size="large" />
          <button class="btn-add-cart" @click="handleAddToCart">加入购物车</button>
          <button class="btn-buy-now" @click="handleBuyNow">立即购买</button>
          <button class="btn-favorite" :class="{ favorited: isFav }" @click="toggleFavorite">
            {{ isFav ? '♥' : '♡' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ═══ 商品详情 ═══ -->
    <div class="detail-section">
      <h3 class="section-heading">商品参数</h3>
      <div v-if="specs.length" class="specs-table">
        <div v-for="s in specs" :key="s.id" class="spec-row">
          <span class="spec-name">{{ s.specName }}</span>
          <span class="spec-value">{{ s.specValue }}</span>
        </div>
      </div>
      <p v-if="product.paramsText && !specs.length" class="params-text">{{ product.paramsText }}</p>
    </div>

    <div v-if="product.detailHtml" class="detail-section">
      <h3 class="section-heading">商品详情</h3>
      <div class="detail-html" v-html="product.detailHtml" />
    </div>

    <!-- ═══ 评价区 ═══ -->
    <div class="detail-section">
      <h3 class="section-heading">商品评价 ({{ reviews.length }})</h3>

      <!-- 评价列表 -->
      <div v-if="reviews.length" class="review-list">
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <div class="review-header">
            <span class="review-user">{{ r.nickname || '匿名用户' }}</span>
            <span class="review-rating">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
            <span class="review-date">{{ r.createdAt?.slice(0, 10) }}</span>
          </div>
          <p class="review-content">{{ r.content }}</p>
          <img v-if="r.imageUrl" :src="r.imageUrl" class="review-image" />
        </div>
      </div>
      <div v-else class="review-empty">暂无评价，成为第一个评价的人～</div>

      <!-- 评价表单 -->
      <div v-if="userStore.isLoggedIn" class="review-form">
        <h4>发表评价</h4>
        <div class="review-rating-input">
          <span>评分：</span>
          <el-rate v-model="reviewForm.rating" />
        </div>
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="3"
          placeholder="分享你的使用体验..."
          maxlength="500"
          show-word-limit
        />
        <el-button type="primary" :loading="submittingReview" @click="handleSubmitReview" style="margin-top:12px">
          提交评价
        </el-button>
      </div>
      <div v-else class="review-login-hint">
        <router-link to="/login">登录</router-link> 后即可评价
      </div>
    </div>
  </div>

  <div v-else class="detail-empty">
    <el-empty description="商品不存在" />
  </div>
</template>

<style scoped>
.detail-root { max-width: 1280px; margin: 0 auto; padding: 20px 16px 60px; }
.detail-loading { max-width: 800px; margin: 40px auto; padding: 0 16px; }

/* ═══ 主体 ═══ */
.detail-main { display: flex; gap: 40px; margin-bottom: 40px; }

/* 图片 */
.detail-gallery { width: 420px; flex-shrink: 0; }
.gallery-main { width: 420px; height: 420px; border-radius: 12px; overflow: hidden; background: #f8f8f8; display: flex; align-items: center; justify-content: center; }
.gallery-main img { max-width: 100%; max-height: 100%; object-fit: contain; }
.gallery-thumbs { display: flex; gap: 8px; margin-top: 12px; }
.gallery-thumbs img { width: 64px; height: 64px; border-radius: 8px; border: 2px solid transparent; cursor: pointer; object-fit: cover; }
.gallery-thumbs img.active { border-color: var(--color-primary); }

/* 信息 */
.detail-info { flex: 1; min-width: 0; }
.info-name { font-size: 22px; font-weight: 700; line-height: 1.4; margin-bottom: 4px; }
.info-subtitle { font-size: 14px; color: #888; margin-bottom: 16px; }
.info-price { margin-bottom: 20px; display: flex; align-items: baseline; gap: 10px; }
.price-current { font-size: 28px; font-weight: 700; color: var(--color-price, #ff0036); }
.price-original { font-size: 15px; color: #bbb; text-decoration: line-through; }
.dummy-meta { display: flex; flex-wrap: wrap; gap: 8px; margin: -8px 0 18px; }
.dummy-meta span { padding: 6px 10px; border-radius: 999px; background: #f8fafc; border: 1px solid #e5e7eb; color: #475569; font-size: 12px; font-weight: 700; }

/* SKU */
.sku-group { margin-bottom: 14px; display: flex; align-items: flex-start; gap: 12px; }
.sku-label { font-size: 13px; color: #888; min-width: 36px; padding-top: 6px; }
.sku-options { display: flex; flex-wrap: wrap; gap: 8px; }
.sku-options button { padding: 6px 16px; border: 1px solid #ddd; border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer; transition: all 0.2s; }
.sku-options button:hover { border-color: var(--color-primary); color: var(--color-primary); }
.sku-options button.active { border-color: var(--color-primary); background: var(--color-primary); color: #fff; }
.sku-options button:disabled { opacity: 0.35; cursor: not-allowed; }

/* 库存 */
.info-stock { margin-bottom: 20px; font-size: 14px; }
.in-stock { color: #22c55e; }
.out-stock { color: #ef4444; }

/* 操作按钮 */
.info-actions { display: flex; align-items: center; gap: 12px; }
.btn-add-cart { height: 44px; padding: 0 28px; border: 2px solid var(--color-primary); border-radius: 22px; background: #fff; color: var(--color-primary); font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.btn-add-cart:hover { background: #fff5f0; }
.btn-buy-now { height: 44px; padding: 0 28px; border: none; border-radius: 22px; background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.btn-buy-now:hover { filter: brightness(1.05); transform: translateY(-1px); }
.btn-favorite { width: 44px; height: 44px; border: 1px solid #ddd; border-radius: 50%; background: #fff; font-size: 20px; cursor: pointer; transition: all 0.2s; flex-shrink: 0; }
.btn-favorite:hover { border-color: #ff6b6b; }
.btn-favorite.favorited { border-color: #ff6b6b; background: #fff0f0; color: #ff4444; }

/* ═══ 详情区 ═══ */
.detail-section { margin-top: 40px; }
.section-heading { font-size: 18px; font-weight: 700; margin-bottom: 16px; padding-left: 12px; border-left: 4px solid var(--color-primary); }
.specs-table { background: #f9fafb; border-radius: 8px; overflow: hidden; }
.spec-row { display: flex; border-bottom: 1px solid #eee; }
.spec-row:last-child { border-bottom: none; }
.spec-name { width: 140px; padding: 10px 16px; font-size: 13px; color: #888; background: #f3f4f6; flex-shrink: 0; }
.spec-value { padding: 10px 16px; font-size: 14px; }
.detail-html { line-height: 1.8; font-size: 15px; }
.detail-html :deep(img) { max-width: 100%; border-radius: 8px; }

/* ═══ 评价 ═══ */
.review-list { margin-bottom: 24px; }
.review-item { padding: 16px 0; border-bottom: 1px solid #f0f0f0; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.review-user { font-weight: 600; font-size: 14px; }
.review-rating { color: #f59e0b; font-size: 13px; }
.review-date { color: #bbb; font-size: 12px; margin-left: auto; }
.review-content { font-size: 14px; color: #444; line-height: 1.6; }
.review-image { max-width: 120px; border-radius: 8px; margin-top: 8px; }
.review-empty { color: #aaa; padding: 24px 0; }
.review-login-hint { color: #aaa; padding: 16px 0; }
.review-login-hint a { color: var(--color-primary); }
.detail-assurance { display: flex; flex-wrap: wrap; gap: 8px; margin: -6px 0 18px; }
.detail-assurance span { padding: 6px 10px; border-radius: 999px; background: #fff5f0; color: var(--color-primary); font-size: 12px; font-weight: 700; }
.review-form { background: #f9fafb; padding: 20px; border-radius: 8px; margin-top: 16px; }
.review-form h4 { margin-bottom: 12px; }
.review-rating-input { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; font-size: 14px; }

.detail-empty { padding: 80px 0; }

/* ═══ 响应式 ═══ */
@media (max-width: 768px) {
  .detail-main { flex-direction: column; gap: 20px; }
  .detail-gallery { width: 100%; }
  .gallery-main { width: 100%; height: auto; aspect-ratio: 1; }
  .info-name { font-size: 18px; }
  .price-current { font-size: 24px; }
  .info-actions { flex-wrap: wrap; }
}
</style>
