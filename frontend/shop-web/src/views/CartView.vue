<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { fetchCart, updateCartQuantity, removeCartItem, type CartItem } from '@/api/cart'
import { imageOrPlaceholder } from '@/utils/image'

const router = useRouter()
const cartStore = useCartStore()

const items = ref<CartItem[]>([])
const checkedIds = ref<Set<number>>(new Set())
const loading = ref(true)

const allChecked = computed({
  get: () => items.value.length > 0 && checkedIds.value.size === items.value.length,
  set: (val: boolean) => {
    checkedIds.value = val ? new Set(items.value.map(i => i.id)) : new Set()
  },
})

const totalPrice = computed(() => {
  let sum = 0
  items.value.forEach(i => { if (checkedIds.value.has(i.id)) sum += i.price * i.quantity })
  return sum.toFixed(2)
})

const totalCount = computed(() => {
  let c = 0
  items.value.forEach(i => { if (checkedIds.value.has(i.id)) c += i.quantity })
  return c
})

async function load() {
  loading.value = true
  try {
    const res: any = await fetchCart()
    items.value = res.data?.items ?? (Array.isArray(res.data) ? res.data : [])
  } catch { items.value = [] }
  finally { loading.value = false }
}

function toggleCheck(id: number) {
  const next = new Set(checkedIds.value)
  next.has(id) ? next.delete(id) : next.add(id)
  checkedIds.value = next
}

async function changeQty(item: CartItem, delta: number) {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  try {
    await updateCartQuantity({ cartItemId: item.id, quantity: newQty })
    item.quantity = newQty
    cartStore.refresh()
  } catch { /* handled */ }
}

async function removeItem(item: CartItem) {
  try {
    await ElMessageBox.confirm(`移除「${item.productName}」？`, '确认', { type: 'warning' })
    await removeCartItem({ cartItemId: item.id })
    items.value = items.value.filter(i => i.id !== item.id)
    checkedIds.value.delete(item.id)
    cartStore.refresh()
    ElMessage.success('已移除')
  } catch { /* cancelled */ }
}

function goCheckout() {
  if (checkedIds.value.size === 0) {
    ElMessage.warning('请先选择商品')
    return
  }
  const ids = Array.from(checkedIds.value).join(',')
  router.push({ path: '/checkout', query: { cartItemIds: ids } })
}

function goProduct(id: number) {
  router.push(`/product/${id}`)
}

onMounted(load)
</script>

<template>
  <div class="page-container cart-page">
    <div class="page-intro">
      <div>
        <h2 class="page-title">我的购物车</h2>
        <p>集中管理已选商品、规格和数量，确认无误后进入订单确认页。</p>
      </div>
    </div>
    <div v-if="items.length" class="page-metrics">
      <div class="metric-card"><span>购物车商品</span><strong>{{ items.length }}</strong><small>当前列表项</small></div>
      <div class="metric-card"><span>已选数量</span><strong>{{ totalCount }}</strong><small>参与结算</small></div>
      <div class="metric-card"><span>结算金额</span><strong>¥{{ totalPrice }}</strong><small>实时合计</small></div>
    </div>

    <div v-if="loading" class="cart-loading">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else-if="items.length === 0" class="cart-empty">
      <el-empty description="购物车是空的">
        <el-button type="primary" @click="router.push('/products')">去逛逛</el-button>
      </el-empty>
    </div>

    <template v-else>
      <div class="cart-header">
        <el-checkbox v-model="allChecked" />
        <span class="header-product">商品信息</span>
        <span class="header-price">单价</span>
        <span class="header-qty">数量</span>
        <span class="header-subtotal">小计</span>
        <span class="header-action">操作</span>
      </div>

      <div v-for="item in items" :key="item.id" class="cart-item">
        <el-checkbox :model-value="checkedIds.has(item.id)" @change="toggleCheck(item.id)" />
        <div class="item-product" @click="goProduct(item.productId)">
          <img :src="imageOrPlaceholder(item.imageUrl)" :alt="item.productName" class="item-img" />
          <div class="item-info">
            <div class="item-name">{{ item.productName }}</div>
            <div v-if="item.color || item.size" class="item-sku">
              <span v-if="item.color" class="sku-tag">{{ item.color }}</span>
              <span v-if="item.size" class="sku-tag">{{ item.size }}</span>
            </div>
          </div>
        </div>
        <div class="item-price">¥{{ item.price }}</div>
        <div class="item-qty">
          <button class="qty-btn" @click="changeQty(item, -1)">−</button>
          <span class="qty-val">{{ item.quantity }}</span>
          <button class="qty-btn" @click="changeQty(item, 1)">+</button>
        </div>
        <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
        <div class="item-action">
          <el-button type="danger" text @click="removeItem(item)">删除</el-button>
        </div>
      </div>

      <div class="cart-footer">
        <el-checkbox v-model="allChecked">全选</el-checkbox>
        <span class="footer-total">已选 <b>{{ totalCount }}</b> 件，合计：<b class="price">¥{{ totalPrice }}</b></span>
        <el-button type="primary" size="large" :disabled="checkedIds.size === 0" @click="goCheckout">去结算</el-button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cart-page { padding-bottom: 120px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }

.cart-header {
  display: flex; align-items: center; gap: 12px; padding: 12px 16px;
  background: #f5f7fa; border-radius: 8px; font-size: 13px; color: #888;
}
.header-product { flex: 1; }
.header-price, .header-qty, .header-subtotal, .header-action { width: 90px; text-align: center; }

.cart-item {
  display: flex; align-items: center; gap: 12px; padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}
.item-product { flex: 1; display: flex; align-items: center; gap: 12px; cursor: pointer; min-width: 0; }
.item-img { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; background: #f8f8f8; flex-shrink: 0; }
.item-name { font-size: 14px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-sku { display: flex; gap: 6px; margin-top: 4px; }
.sku-tag { padding: 1px 8px; background: #f0f0f0; border-radius: 4px; font-size: 12px; color: #666; }
.item-price { width: 90px; text-align: center; font-size: 15px; font-weight: 600; }
.item-qty { width: 90px; display: flex; align-items: center; justify-content: center; gap: 6px; }
.qty-btn {
  width: 28px; height: 28px; border: 1px solid #ddd; border-radius: 6px;
  background: #fff; font-size: 16px; cursor: pointer; display: flex; align-items: center; justify-content: center;
}
.qty-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.qty-val { font-size: 15px; font-weight: 600; min-width: 20px; text-align: center; }
.item-subtotal { width: 90px; text-align: center; font-size: 15px; font-weight: 600; color: var(--color-price, #ff0036); }
.item-action { width: 90px; text-align: center; }

.cart-footer {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  border-top: 2px solid var(--color-primary);
  padding: 14px 24px; display: flex; align-items: center; justify-content: space-between; gap: 16px; z-index: 50;
}
.footer-total { font-size: 14px; color: #666; }
.footer-total .price { color: var(--color-price, #ff0036); font-size: 20px; }

@media (max-width: 768px) {
  .cart-header { display: none; }
  .cart-item { flex-wrap: wrap; gap: 8px; position: relative; padding-right: 50px; }
  .item-price { width: auto; font-size: 13px; }
  .item-subtotal { width: auto; font-size: 13px; }
  .item-qty { width: auto; }
  .item-action { position: absolute; top: 16px; right: 0; width: auto; }
  .cart-footer { flex-wrap: wrap; gap: 10px; padding: 12px 16px; }
}
</style>
