<template>
  <ShopLayout>
    <div class="page-wrap">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>全部商品</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 筛选卡片 -->
      <section class="filter-card">
        <div class="filter-row">
          <span class="label">分类</span>
          <div class="category-pills">
            <button :class="{ active: !filters.categoryId }" @click="setCategory(null)">全部</button>
            <button v-for="c in categories" :key="c.id" :class="{ active: filters.categoryId === c.id }" @click="setCategory(c.id)">
              {{ c.name }}
            </button>
          </div>
        </div>
        <div class="filter-row filter-bottom">
          <div class="list-search">
            <el-input v-model="filters.keyword" placeholder="搜索商品名称" @keyup.enter="load">
              <template #append><el-button @click="load">搜索</el-button></template>
            </el-input>
          </div>
          <el-segmented v-model="filters.searchMode" :options="searchModeOptions" @change="searchAgain" />
          <div class="price-filter">
            <el-input-number v-model="minPrice" :min="0" placeholder="最低价" controls-position="right" />
            <span class="price-gap">-</span>
            <el-input-number v-model="maxPrice" :min="0" placeholder="最高价" controls-position="right" />
            <el-button type="danger" size="small" @click="load">筛选</el-button>
          </div>
          <div class="sort-tabs">
            <button v-for="option in sortOptions" :key="option.value" :class="{ active: filters.sort === option.value }" @click="setSort(option.value)">
              {{ option.label }}
            </button>
          </div>
        </div>
      </section>

      <div class="result-bar">
        <strong>商品列表</strong>
        <span>共 {{ total }} 件商品，当前第 {{ filters.page }} 页</span>
      </div>

      <section v-if="products.length" class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" @add="add" @favorite="favorite" />
      </section>
      <EmptyState v-else title="暂无符合条件的商品" description="请调整分类、关键词或价格范围后再试。" />

      <div v-if="total > filters.size" class="pager">
        <el-pagination layout="prev, pager, next, total" :total="total" :page-size="filters.size" :current-page="filters.page" @current-change="changePage" />
      </div>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useUserStore, useCartStore, useFavoriteStore } from '@/stores'
import ShopLayout from '../layouts/ShopLayout.vue'
import ProductCard from '../components/ProductCard.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute()
const userStore = useUserStore()
const products = ref<any[]>([])
const total = ref(0)
const categories = ref<any[]>([])
const minPrice = ref<any>(null)
const maxPrice = ref<any>(null)
const filters = ref({ categoryId: null, keyword: '', searchMode: 'fuzzy', sort: 'default', page: 1, size: 12 })
const searchModeOptions = [
  { label: '模糊', value: 'fuzzy' },
  { label: '精准', value: 'exact' }
]
const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '新品', value: 'newest' },
  { label: '销量', value: 'sales_desc' },
  { label: '价格升序', value: 'price_asc' },
  { label: '价格降序', value: 'price_desc' }
]

async function load() {
  const params: Record<string, any> = { page: filters.value.page, size: filters.value.size, sort: filters.value.sort }
  if (filters.value.categoryId) params.categoryId = filters.value.categoryId
  if (filters.value.keyword) params.keyword = filters.value.keyword
  if (filters.value.keyword) params.searchMode = filters.value.searchMode
  if (Number(minPrice.value) > 0) params.minPrice = minPrice.value
  if (Number(maxPrice.value) > 0) params.maxPrice = maxPrice.value
  const result = (await api.get('/products', { params })).data.data
  products.value = result.items || []
  total.value = result.total || 0
}

function changePage(page) {
  filters.value.page = page
  scrollTo({ top: 0, behavior: 'smooth' })
  load()
}
function setCategory(categoryId) { filters.value.categoryId = categoryId; filters.value.page = 1; load() }
function setSort(sort) { filters.value.sort = sort; filters.value.page = 1; load() }
function searchAgain() { filters.value.page = 1; load() }

async function add(product) {
  if (!userStore.userId) return ElMessage.warning('请先登录后加入购物车')
  await api.post('/cart/items', { userId: userStore.userId, productId: product.id, quantity: 1 })
  ElMessage.success('已加入购物车')
}
async function favorite(product) {
  if (!userStore.userId) return ElMessage.warning('请先登录后收藏')
  await api.post(`/favorites/${product.id}`, null, { params: { userId: userStore.userId } })
  ElMessage.success('已收藏商品')
}

onMounted(async () => {
  categories.value = (await api.get('/categories')).data.data || []
  filters.value.categoryId = route.query.categoryId ? Number(route.query.categoryId) : null as any
  filters.value.keyword = String(route.query.keyword || '')
  filters.value.sort = String(route.query.sort || 'default')
  load()
})
</script>

<style scoped>
.breadcrumb {
  margin-bottom: 14px;
}

.filter-card {
  display: grid;
  gap: 16px;
  padding: 18px;
  margin-bottom: 18px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.filter-row {
  display: flex;
  gap: 14px;
  align-items: center;
}

.label {
  flex: 0 0 auto;
  color: var(--muted);
  font-weight: 700;
  font-size: 14px;
  min-width: 40px;
}

.category-pills,
.sort-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-pills button,
.sort-tabs button {
  cursor: pointer;
  background: #fff;
  border: 0;
  transition: color .2s, background .2s, border-color .2s;
}

.category-pills button {
  padding: 7px 18px;
  color: var(--muted);
  border: 1px solid var(--line);
  border-radius: 999px;
  font-size: 14px;
}

.category-pills button.active,
.category-pills button:hover {
  color: var(--brand);
  background: var(--brand-light);
  border-color: var(--brand);
}

.filter-bottom {
  flex-wrap: wrap;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid #f1f5f9;
}

.list-search {
  width: min(360px, 100%);
}
.list-search :deep(.el-input__wrapper) {
  border-radius: 999px 0 0 999px;
}
.list-search :deep(.el-input-group__append) {
  overflow: hidden;
  background: var(--brand);
  border-color: var(--brand);
  border-radius: 0 999px 999px 0;
}
.list-search :deep(.el-input-group__append .el-button) {
  color: #fff;
  background: var(--brand);
  border-color: var(--brand);
  padding: 0 18px;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}
.price-filter :deep(.el-input-number) {
  width: 120px;
}
.price-gap {
  color: var(--muted);
}

.sort-tabs button {
  padding: 8px 2px;
  color: var(--muted);
  font-weight: 650;
  font-size: 14px;
  border-bottom: 2px solid transparent;
}
.sort-tabs button.active,
.sort-tabs button:hover {
  color: var(--brand);
  border-bottom-color: var(--brand);
}

.result-bar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  color: var(--muted);
  font-size: 14px;
}
.result-bar strong {
  color: var(--ink);
  font-size: 20px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

@media (max-width: 1100px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
  .filter-row { align-items: flex-start; flex-direction: column; }
  .price-filter, .list-search { width: 100%; }
  .price-filter :deep(.el-input-number) { width: calc((100% - 40px) / 2); }
}
</style>
