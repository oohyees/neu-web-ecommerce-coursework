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
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">{{ categoryName || '商品列表' }}</h2>
    <div v-if="loading"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="!products.length" class="empty"><el-empty description="该分类暂无商品" /></div>
    <div v-else class="product-grid">
      <div v-for="p in products" :key="p.id" class="product-card" @click="router.push(`/product/${p.id}`)">
        <div class="product-img-box"><img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.name" /></div>
        <div class="product-info"><h3>{{ p.name }}</h3><span class="price">¥{{ p.price }}</span></div>
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
.product-info h3 { font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 6px; }
.price { font-size: 16px; font-weight: 700; color: var(--color-price, #ff0036); }
@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } }
</style>
