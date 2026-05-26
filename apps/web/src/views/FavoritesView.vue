<template>
  <ShopLayout>
    <div class="page-wrap">
      <section class="fav-head">
        <div>
          <h1>我的收藏</h1>
          <p>展示所有已收藏的商品。</p>
        </div>
        <el-button @click="$router.push('/products')">继续逛</el-button>
      </section>
      <section v-if="products.length" class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" @add="addToCart" @favorite="unfavorite" />
      </section>
      <EmptyState v-else title="暂无收藏商品" description="浏览商品时点击心形图标即可收藏。">
        <el-button type="danger" @click="$router.push('/products')">去逛逛</el-button>
      </EmptyState>
    </div>
  </ShopLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useSessionStore } from '../store'
import ShopLayout from '../layouts/ShopLayout.vue'
import ProductCard from '../components/ProductCard.vue'
import EmptyState from '../components/EmptyState.vue'

const products = ref([]), session = useSessionStore()

async function load() { products.value = (await api.get('/favorites', { params: { userId: session.userId } })).data.data || [] }

async function addToCart(product) {
  await api.post('/cart/items', { userId: session.userId, productId: product.id, quantity: 1 })
  ElMessage.success('已加入购物车')
}

async function unfavorite(product) {
  await api.delete(`/favorites/${product.id}`, { params: { userId: session.userId } })
  ElMessage.success('已取消收藏')
  load()
}

onMounted(load)
</script>

<style scoped>
.fav-head { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 20px; }
h1, p { margin: 0; }
p { color: var(--muted); margin-top: 4px; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
@media (max-width: 1100px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>
