<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchProducts, type Product } from '@/api/product'
import { imageOrPlaceholder } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const keyword = ref((route.query.keyword as string) || '')
const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const sort = ref('')

async function search() {
  loading.value = true
  try {
    const res: any = await fetchProducts({ keyword: keyword.value, searchMode: 'fuzzy', sort: sort.value || undefined, page: page.value, size: 12 })
    products.value = res.data?.items ?? []
    total.value = res.data?.total ?? 0
  } catch { products.value = [] }
  finally { loading.value = false }
}

function goSearch() {
  const kw = keyword.value.trim()
  if (kw) { router.replace({ path: '/search', query: { keyword: kw } }); page.value = 1; search() }
}

onMounted(() => { if (keyword.value) search() })
watch(page, search)
watch(() => route.query.keyword, (v) => { if (v) { keyword.value = v as string; page.value = 1; search() } })
</script>

<template>
  <div class="page-container">
    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索商品..." class="search-input" @keydown.enter="goSearch" />
      <el-button type="primary" @click="goSearch">搜索</el-button>
    </div>
    <div class="search-toolbar" v-if="products.length">
      <span>共 {{ total }} 个结果</span>
      <el-radio-group v-model="sort" size="small" @change="search">
        <el-radio-button value="">默认</el-radio-button>
        <el-radio-button value="price_asc">价格↑</el-radio-button>
        <el-radio-button value="price_desc">价格↓</el-radio-button>
        <el-radio-button value="sales_desc">销量</el-radio-button>
      </el-radio-group>
    </div>
    <div v-if="loading"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="!products.length && keyword" class="empty"><el-empty description="未找到相关商品" /></div>
    <div v-else class="product-grid">
      <div v-for="p in products" :key="p.id" class="product-card" @click="router.push(`/product/${p.id}`)">
        <div class="product-img-box"><img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.name" /></div>
        <div class="product-info"><h3>{{ p.name }}</h3><span class="price">¥{{ p.price }}</span></div>
      </div>
    </div>
    <el-pagination v-if="total > 12" v-model:current-page="page" :page-size="12" :total="total" layout="prev, pager, next" style="margin-top:20px;justify-content:center" />
  </div>
</template>
<style scoped>
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.search-input { flex: 1; height: 44px; padding: 0 16px; border: 2px solid var(--color-primary); border-radius: 8px; font-size: 15px; outline: none; }
.search-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; font-size: 13px; color: #888; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: #fff; border-radius: 12px; overflow: hidden; cursor: pointer; box-shadow: 0 1px 4px rgba(0,0,0,0.04); transition: all 0.2s; }
.product-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); transform: translateY(-2px); }
.product-img-box { aspect-ratio: 1; background: #f8f8f8; display: flex; align-items: center; justify-content: center; }
.product-img-box img { max-width: 100%; max-height: 100%; object-fit: contain; }
.product-info { padding: 12px; }
.product-info h3 { font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 6px; }
.price { font-size: 16px; font-weight: 700; color: var(--color-price, #ff0036); }
@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } }
</style>
