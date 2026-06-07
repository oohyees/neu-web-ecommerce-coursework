<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchProducts, fetchCategories, type Product, type Category } from '@/api/product'
import { imageOrPlaceholder } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const categoryId = ref(Number(route.params.id))
const categoryName = ref('')
const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const sort = ref('')
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const [prodRes, catRes] = await Promise.all([
      fetchProducts({ categoryId: categoryId.value, sort: sort.value || undefined, size: 20 }),
      fetchCategories(),
    ])
    products.value = (prodRes as any).data?.items ?? []
    categories.value = (catRes as any).data ?? catRes
    const cat = categories.value.find(c => c.id === categoryId.value)
    categoryName.value = cat?.name || ''
  } catch { products.value = [] }
  finally { loading.value = false }
}

onMounted(load)
watch(() => route.params.id, (v) => { categoryId.value = Number(v); load() })

function productParam(product: Product, name: string) {
  if (!product.paramsText) return ''
  const item = product.paramsText.split(';').find((part) => part.trim().startsWith(`${name}:`))
  return item?.split(':').slice(1).join(':').trim() || ''
}
</script>

<template>
  <div class="page-container">
    <div class="page-intro">
      <div>
        <h2 class="page-title">{{ categoryName || '商品列表' }}</h2>
        <p>按分类筛选商品，图片、价格和详情入口保持一致，便于从分类进入购买链路。</p>
      </div>
    </div>
    <div class="page-metrics">
      <div class="metric-card"><span>当前分类</span><strong>{{ categoryName || '全部' }}</strong><small>分类筛选</small></div>
      <div class="metric-card"><span>商品数量</span><strong>{{ products.length }}</strong><small>当前结果</small></div>
      <div class="metric-card"><span>展示方式</span><strong>卡片</strong><small>图片优先</small></div>
    </div>
    <div v-if="loading"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="!products.length" class="empty"><el-empty description="该分类暂无商品" /></div>
    <div v-else class="product-grid">
      <div v-for="p in products" :key="p.id" class="product-card" @click="router.push(`/product/${p.id}`)">
        <div class="product-img-box"><img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.name" /></div>
        <div class="product-info">
          <div class="product-tags">
            <span>{{ p.brand || productParam(p, '品牌') || productParam(p, '分类') || 'DummyJSON' }}</span>
            <span v-if="p.rating || productParam(p, '评分')">★ {{ p.rating || productParam(p, '评分') }}</span>
          </div>
          <h3>{{ p.name }}</h3>
          <p v-if="p.subtitle">{{ p.subtitle }}</p>
          <div class="price-row">
            <span class="price">¥{{ p.price }}</span>
            <span v-if="p.discountPercentage || productParam(p, '折扣')" class="discount">-{{ p.discountPercentage || productParam(p, '折扣') }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: #fff; border-radius: 12px; overflow: hidden; cursor: pointer; box-shadow: 0 1px 4px rgba(0,0,0,0.04); transition: all 0.2s; }
.product-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); transform: translateY(-2px); }
.product-img-box { aspect-ratio: 1; background: #f8f8f8; display: flex; align-items: center; justify-content: center; }
.product-img-box img { max-width: 100%; max-height: 100%; object-fit: contain; }
.product-info { padding: 12px; }
.product-tags { display: flex; gap: 6px; margin-bottom: 8px; min-width: 0; }
.product-tags span { padding: 3px 7px; border-radius: 999px; background: #fff5f0; color: var(--color-primary); font-size: 11px; font-weight: 700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-info h3 { font-size: 13px; line-height: 1.45; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; min-height: 38px; margin-bottom: 6px; }
.product-info p { color: #8a94a6; font-size: 12px; line-height: 1.45; height: 34px; margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.price-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.price { font-size: 16px; font-weight: 700; color: var(--color-price, #ff0036); }
.discount { color: #ef4444; font-size: 12px; font-weight: 700; }
@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } }
</style>
