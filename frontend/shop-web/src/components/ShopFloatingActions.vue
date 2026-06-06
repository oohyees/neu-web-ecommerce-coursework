<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const showBackTop = ref(false)

function onScroll() { showBackTop.value = window.scrollY > 400 }
function scrollTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }
function goCart() { router.push('/cart') }
function goService() { router.push('/service') }
function goTop() { scrollTop() }

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <Teleport to="body">
    <div class="floating-bar">
      <div class="float-item cart-item" @click="goCart">
        <svg viewBox="0 0 24 24" class="float-icon" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
        </svg>
        <span v-if="cartStore.itemCount > 0" class="float-badge">{{ cartStore.itemCount > 99 ? '99+' : cartStore.itemCount }}</span>
        <span class="float-label">购物车</span>
      </div>
      <div class="float-item" @click="goService">
        <svg viewBox="0 0 24 24" class="float-icon" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="float-label">客服</span>
      </div>
      <div v-if="showBackTop" class="float-item back-top" @click="goTop">
        <svg viewBox="0 0 24 24" class="float-icon" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="18 15 12 9 6 15"/>
        </svg>
        <span class="float-label">顶部</span>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.floating-bar {
  position: fixed; right: 16px; bottom: 120px; z-index: 999;
  display: flex; flex-direction: column; gap: 4px;
}
.float-item {
  width: 48px; height: 48px; background: #fff; border-radius: 12px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; box-shadow: 0 2px 12px rgba(0,0,0,0.08); position: relative;
  transition: all 0.2s; color: #666;
}
.float-item:hover { color: var(--color-primary); box-shadow: 0 4px 20px rgba(255,107,53,0.2); transform: translateY(-2px); }
.float-icon { width: 22px; height: 22px; }
.float-label { font-size: 10px; color: #999; margin-top: 1px; }
.float-badge {
  position: absolute; top: -4px; right: -4px; min-width: 18px; height: 18px;
  background: var(--color-price, #ff0036); color: #fff; font-size: 10px; font-weight: 700;
  border-radius: 9px; display: flex; align-items: center; justify-content: center; padding: 0 4px;
}
.back-top { animation: fadeInUp 0.3s ease; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 768px) {
  .floating-bar { right: 8px; bottom: 100px; }
  .float-item { width: 42px; height: 42px; border-radius: 10px; }
}
</style>
