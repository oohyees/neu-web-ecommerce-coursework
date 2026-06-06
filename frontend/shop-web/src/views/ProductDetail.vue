<template>
  <ShopLayout>
    <div class="page-wrap" v-if="product">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/products' }">商品</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="detail-panel">
        <div class="gallery">
          <img :src="product.imageUrl" :alt="product.name" @error="imgFallback" />
        </div>
        <div class="summary">
          <h1>{{ product.name }}</h1>
          <p class="sub muted">销量 {{ product.sales || 0 }} · 库存 {{ product.stock ?? 0 }}件 · {{ favorite ? '已收藏' : '可收藏' }}</p>

          <div class="price-box">
            <span v-if="flashSale" class="flash-tag">秒杀</span>
            <strong class="price">&yen;{{ flashSale ? flashSale.promotionPrice : product.price }}</strong>
            <del v-if="flashSale" class="muted">&yen;{{ product.price }}</del>
          </div>
          <el-alert v-if="flashSale" title="限时秒杀进行中，库存有限，速抢！" type="error" :closable="false" show-icon />

          <el-form label-width="72px" class="buy-form">
            <el-form-item v-for="(values, name) in groupedSpecs" :key="name" :label="name">
              <el-radio-group v-model="selectedSpecs[name]" size="small">
                <el-radio-button v-for="value in values" :key="value" :label="value" :value="value" />
              </el-radio-group>
            </el-form-item>
            <el-form-item label="数量">
              <el-input-number v-model="quantity" :min="1" :max="product.stock || 99" />
              <span class="stock-tip muted">库存 {{ product.stock ?? 0 }} 件</span>
            </el-form-item>
          </el-form>

          <div class="action-row">
            <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
            <el-button size="large" @click="add">加入购物车</el-button>
            <el-button size="large" @click="toggleFavorite">{{ favorite ? '♥ 已收藏' : '♡ 收藏' }}</el-button>
          </div>

          <ul class="service-list">
            <li>✓ 正品保障</li>
            <li>✓ 极速发货</li>
            <li>✓ 7天无理由</li>
            <li>✓ 客服咨询</li>
          </ul>
        </div>
      </section>

      <div class="detail-bottom">
        <el-tabs model-value="detail">
          <el-tab-pane label="商品详情" name="detail">
            <div class="tab-panel">{{ product.detailHtml || '暂无详情介绍，敬请期待更多内容。' }}</div>
          </el-tab-pane>
          <el-tab-pane label="规格参数" name="params">
            <el-descriptions :column="1" border class="params-table">
              <el-descriptions-item label="参数">{{ product.paramsText || '暂无参数' }}</el-descriptions-item>
              <el-descriptions-item label="商品编号">{{ product.id }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane :label="`用户评价 (${reviews.length})`" name="reviews">
            <section class="reviews-section">
              <article v-for="r in reviews" :key="r.id" class="review-card">
                <div class="review-head">
                  <el-rate :model-value="r.rating" disabled size="small" />
                  <span class="muted">{{ r.rating }} 分</span>
                </div>
                <p>{{ r.content }}</p>
                <img v-if="r.imageUrl" :src="r.imageUrl" class="review-img" />
              </article>
              <EmptyState v-if="!reviews.length" title="暂无评价" description="购买后可上传图片评价。" />
              <div v-if="reviewTotal > reviewSize" class="review-pager">
                <el-pagination layout="prev, pager, next, total" :total="reviewTotal" :page-size="reviewSize" :current-page="reviewPage" @current-change="changeReviewPage" />
              </div>

              <el-divider />
              <h3>发表评价</h3>
              <el-form :model="reviewForm" class="review-form" label-width="72px">
                <el-form-item label="评分"><el-rate v-model="reviewForm.rating" /></el-form-item>
                <el-form-item label="评价"><el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="分享你的使用体验..." /></el-form-item>
                <el-form-item label="图片">
                  <el-upload :show-file-list="false" :http-request="uploadImage"><el-button size="small">上传图片</el-button></el-upload>
                  <img v-if="reviewForm.imageUrl" :src="reviewForm.imageUrl" class="upload-preview" />
                </el-form-item>
                <el-button type="danger" @click="submitReview">提交评价</el-button>
              </el-form>
            </section>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useUserStore, useCartStore, useFavoriteStore } from '@/stores'
import ShopLayout from '../layouts/ShopLayout.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute(), router = useRouter(), session = useUserStore() as any as any
const product = ref<any>(null), reviews = ref<any[]>([]), favorite = ref(false), specs = ref<any[]>([])
const selectedSpecs = ref({}), flashSale = ref<any>(null), quantity = ref(1)
const reviewPage = ref(1), reviewSize = 5, reviewTotal = ref(0)
const reviewForm = ref({ rating: 5, content: '', imageUrl: '' })
const groupedSpecs = computed(() => specs.value.reduce((m, s) => ((m[s.specName] ??= []).push(s.specValue), m), {}))
const specText = computed(() => Object.entries(selectedSpecs.value).map(([k, v]) => `${k}:${v}`).join(' / '))

async function load() {
  product.value = (await api.get(`/products/${route.params.id}`)).data.data
  await loadReviews()
  specs.value = (await api.get(`/marketing/specs/${route.params.id}`)).data.data || []
  flashSale.value = (await api.get('/marketing/promotions')).data.data.find(p => p.productId === Number(route.params.id) && p.promotionType === 'FLASH_SALE')
  if (useUserStore().userId) favorite.value = (await api.get(`/favorites/${route.params.id}/status`, { params: { userId: useUserStore().userId } })).data.data
  selectedSpecs.value = {}
}

async function loadReviews() {
  const result = (await api.get('/reviews', { params: { productId: route.params.id, page: reviewPage.value, size: reviewSize } })).data.data || {}
  reviews.value = result.items || []
  reviewTotal.value = result.total || 0
}

function changeReviewPage(v) { reviewPage.value = v; loadReviews() }

async function add() {
  if (!useUserStore().userId) return ElMessage.warning('请先登录') && false
  if (!specsReady.value) return ElMessage.warning('请选择规格') && false
  await api.post('/cart/items', { userId: useUserStore().userId, productId: Number(route.params.id), quantity: quantity.value, specText: specText.value })
  ElMessage.success('已加入购物车')
  return true
}
async function buyNow() {
  const ok = await add()
  if (!ok) return
  const currentSpecText = specText.value
  const productId = Number(route.params.id)
  const cartItems = (await api.get('/cart')).data.data || []
  const matchedItem = cartItems.find(i => i.productId === productId && (i.specText || '') === (currentSpecText || ''))
  if (!matchedItem) {
    ElMessage.error('未能定位刚加入购物车的商品，请稍后重试')
    return
  }
  sessionStorage.setItem('checkoutCartItemIds', JSON.stringify([matchedItem.id]))
  sessionStorage.removeItem('checkoutProductIds')
  router.push('/checkout')
}
async function toggleFavorite() {
  if (!useUserStore().userId) return ElMessage.warning('请先登录')
  if (favorite.value) { await api.delete(`/favorites/${route.params.id}`, { params: { userId: useUserStore().userId } }); ElMessage.success('已取消收藏') }
  else { await api.post(`/favorites/${route.params.id}`, null, { params: { userId: useUserStore().userId } }); ElMessage.success('已收藏') }
  favorite.value = !favorite.value
}
async function uploadImage({ file }) { const fd = new FormData(); fd.append('file', file); reviewForm.value.imageUrl = (await api.post('/files/upload', fd)).data.data }
async function submitReview() {
  if (!useUserStore().userId) return ElMessage.warning('请先登录')
  await api.post('/reviews', { userId: useUserStore().userId, productId: Number(route.params.id), ...reviewForm.value })
  ElMessage.success('评价已提交')
  reviewForm.value = { rating: 5, content: '', imageUrl: '' }
  reviewPage.value = 1
  load()
}
const specsReady = computed(() => Object.keys(groupedSpecs.value).every(name => selectedSpecs.value[name]))
function imgFallback(e: Event) {
  (e.target as HTMLImageElement).src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 400"><rect fill="%23f3f4f6" width="400" height="400"/><text x="200" y="200" text-anchor="middle" dy=".35em" fill="%239ca3af" font-size="18">暂无图片</text></svg>'
}

onMounted(load)
</script>

<style scoped>
.breadcrumb { margin-bottom: 16px; }
.detail-panel {
  display: grid;
  grid-template-columns: 460px minmax(0, 1fr);
  gap: 28px;
  padding: 24px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

.gallery {
  height: 440px;
  padding: 20px;
  background: #f6f6f6;
  border-radius: var(--radius);
  display: grid;
  place-items: center;
}
.gallery img { max-width: 100%; max-height: 100%; object-fit: contain; }

h1 { margin: 0 0 8px; font-size: 24px; line-height: 1.35; }
.sub { font-size: 14px; }

.price-box {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 18px 20px;
  margin: 18px 0;
  background: var(--brand-light);
  border-radius: var(--radius);
}
.flash-tag {
  padding: 2px 8px;
  color: #fff;
  background: var(--brand);
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
}
.price-box strong { font-size: 32px; font-weight: 800; }
.price-box del { font-size: 16px; }

.buy-form { margin-top: 18px; }
.stock-tip { margin-left: 12px; font-size: 13px; }

.action-row { display: flex; flex-wrap: wrap; gap: 12px; margin: 22px 0; }

.service-list {
  display: flex; flex-wrap: wrap; gap: 10px; padding: 0; margin: 0;
  list-style: none;
}
.service-list li {
  padding: 6px 12px; color: var(--muted); font-size: 13px;
  background: #f8fafc; border-radius: 999px;
}

.detail-bottom {
  margin-top: 20px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}
.tab-panel { padding: 16px 0; line-height: 1.8; min-height: 80px; }
.params-table { margin-top: 12px; }

.reviews-section { padding: 8px 0; }
.review-card { padding: 16px; margin-bottom: 12px; background: #f8fafc; border-radius: var(--radius); }
.review-head { display: flex; gap: 10px; align-items: center; margin-bottom: 8px; }
.review-card p { margin: 8px 0; line-height: 1.6; }
.review-img { width: 100px; height: 75px; object-fit: cover; border-radius: 6px; }
.review-pager { display: flex; justify-content: flex-end; margin: 12px 0; }
.review-form { max-width: 560px; margin-top: 16px; }
.review-form h3 { margin: 0 0 12px; font-size: 16px; }
.upload-preview { width: 100px; height: 75px; object-fit: cover; border-radius: 6px; margin-top: 6px; }

@media (max-width: 900px) {
  .detail-panel { grid-template-columns: 1fr; }
  .gallery { height: 300px; }
}
</style>
