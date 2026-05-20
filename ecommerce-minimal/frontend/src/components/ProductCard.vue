<template>
  <article class="product-card" @click="$router.push(`/products/${product.id}`)">
    <div class="image-box">
      <img :src="product.imageUrl" :alt="product.name" @error="imgFail" />
      <span v-if="badge" class="badge">{{ badge }}</span>
      <button class="fav-btn" :class="{ liked: liked }" type="button" @click.stop="toggleFav">
        {{ liked ? '♥' : '♡' }}
      </button>
    </div>
    <div class="content">
      <h3>{{ product.name }}</h3>
      <div class="meta">
        <span>已售 {{ product.sales || 0 }}</span>
        <span v-if="product.rating">★ {{ product.rating }}</span>
      </div>
      <div class="buy-row">
        <span class="price">&yen;{{ product.price }}</span>
      </div>
      <el-button class="add-btn" size="large" @click.stop="$emit('add', product)">加入购物车</el-button>
    </div>
  </article>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  product: { type: Object, required: true },
  badge: { type: String, default: '' }
})
const emit = defineEmits(['add', 'favorite'])

const liked = ref(false)

watch(() => props.product?.id, () => { liked.value = false })

function toggleFav() {
  liked.value = !liked.value
  emit('favorite', props.product)
}

function imgFail(e) {
  e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200"><rect fill="%23f3f4f6" width="200" height="200"/><text x="100" y="100" text-anchor="middle" dy=".35em" fill="%239ca3af" font-size="14">暂无图片</text></svg>'
}
</script>

<style scoped>
.product-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  transition: border-color .2s, box-shadow .2s, transform .25s;
}

.product-card:hover {
  border-color: #f3b3be;
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.image-box {
  position: relative;
  height: 210px;
  padding: 12px;
  background: #f6f6f6;
}

img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  background: var(--brand);
  border-radius: 999px;
}

.fav-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  color: var(--muted);
  cursor: pointer;
  background: rgba(255, 255, 255, .9);
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  font-size: 18px;
  line-height: 1;
  transition: color .2s, border-color .2s;
}

.fav-btn:hover,
.fav-btn.liked {
  color: var(--brand);
  border-color: var(--brand);
}

.content {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 14px;
  border-top: 1px solid #f1f5f9;
}

h3 {
  display: -webkit-box;
  min-height: 42px;
  margin: 0 0 10px;
  overflow: hidden;
  font-size: 15px;
  font-weight: 650;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--muted);
  font-size: 13px;
}

.price {
  display: block;
  margin: 8px 0 0;
  color: var(--brand);
  font-size: 20px;
  font-weight: 800;
}

.add-btn {
  width: 100%;
  margin-top: auto;
  padding-top: 10px;
  color: #fff !important;
  background: var(--brand) !important;
  border-color: var(--brand) !important;
  border-radius: 999px !important;
  font-weight: 700 !important;
  font-size: 14px !important;
}

.add-btn:hover {
  background: var(--brand-dark) !important;
  border-color: var(--brand-dark) !important;
}

@media (max-width: 540px) {
  .image-box {
    height: 150px;
    padding: 10px;
  }
  .content {
    padding: 10px;
  }
  h3 {
    min-height: 38px;
    font-size: 13px;
    line-height: 1.4;
  }
  .meta {
    font-size: 12px;
  }
  .price {
    font-size: 17px;
  }
  .add-btn {
    min-height: 36px;
    padding: 8px 10px !important;
    font-size: 13px !important;
  }
}
</style>
