<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useFavoriteStore } from '@/stores/favorite'
import { fetchFavorites, type Product } from '@/api/product'
import { imageOrPlaceholder } from '@/utils/image'

const router = useRouter()
const favoriteStore = useFavoriteStore()
const items = ref<Product[]>([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const res: any = await fetchFavorites()
    items.value = res.data ?? []
  } catch { items.value = [] }
  finally { loading.value = false }
}

async function removeFav(productId: number) {
  await favoriteStore.toggle(productId)
  items.value = items.value.filter(i => i.id !== productId)
}

function goProduct(id: number) { router.push(`/product/${id}`) }

onMounted(load)
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">我的收藏</h2>
    <div v-if="loading"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="!items.length" class="empty"><el-empty description="暂无收藏" /></div>
    <div v-else class="product-grid">
      <div v-for="p in items" :key="p.id" class="product-card" @click="goProduct(p.id)">
        <div class="product-img-box"><img :src="imageOrPlaceholder(p.imageUrl)" :alt="p.name" /></div>
        <div class="product-info">
          <h3 class="product-name">{{ p.name }}</h3>
          <div class="product-price">¥{{ p.price }}</div>
        </div>
        <el-button size="small" type="danger" text @click.stop="removeFav(p.id)">取消收藏</el-button>
      </div>
    </div>
  </div>
</template>
<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: #fff; border-radius: 12px; padding: 12px; cursor: pointer; box-shadow: 0 1px 4px rgba(0,0,0,0.04); transition: all 0.2s; text-align: center; }
.product-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); transform: translateY(-2px); }
.product-img-box { aspect-ratio: 1; display: flex; align-items: center; justify-content: center; background: #f8f8f8; border-radius: 8px; overflow: hidden; margin-bottom: 8px; }
.product-img-box img { max-width: 100%; max-height: 100%; object-fit: contain; }
.product-name { font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px; }
.product-price { font-size: 16px; font-weight: 700; color: var(--color-price, #ff0036); }
@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } }
</style>
