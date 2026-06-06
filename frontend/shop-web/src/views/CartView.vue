<template>
  <ShopLayout>
    <div class="page-wrap">
      <el-steps :active="0" finish-status="success" class="steps">
        <el-step title="购物车" />
        <el-step title="确认订单" />
        <el-step title="支付完成" />
      </el-steps>

      <section class="cart-head">
        <div>
          <h1>购物车</h1>
          <p>勾选商品后进入结算，支持修改数量和删除商品。</p>
        </div>
        <el-button @click="$router.push('/products')">继续购物</el-button>
      </section>

      <section class="bulk">
        <el-button link @click="selectAll">全选</el-button>
        <el-button link @click="invertSelection">反选</el-button>
        <span class="muted">共 {{ items.length }} 件商品</span>
      </section>

      <!-- 桌面端表格 -->
      <el-table v-if="items.length" ref="tableRef" class="desktop-only cart-table" :data="items" @selection-change="changeSelection">
        <el-table-column type="selection" width="50" />
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="goods-cell">
              <img :src="row.imageUrl" @error="imgFallback" />
              <div>
                <strong>{{ row.productName }}</strong>
                <span>{{ row.specText || '默认规格' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }"><span class="price">&yen;{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-input-number :model-value="row.quantity" :min="1" :max="99" size="small" @change="value => updateQuantity(row, value)" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }"><span class="price">&yen;{{ row.subtotal }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button type="danger" link @click="removeItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 移动端列表 -->
      <section class="mobile-cart mobile-only">
        <article v-for="row in items" :key="row.id" class="mobile-item">
          <el-checkbox :model-value="selected.includes(row)" @change="checked => toggleMobile(row, checked)" />
          <img :src="row.imageUrl" @error="imgFallback" />
          <div>
            <strong>{{ row.productName }}</strong>
            <span>{{ row.specText || '默认规格' }}</span>
            <div class="mobile-price-qty">
              <b class="price">&yen;{{ row.subtotal }}</b>
              <el-input-number :model-value="row.quantity" :min="1" size="small" @change="value => updateQuantity(row, value)" />
            </div>
            <el-button type="danger" link size="small" @click="removeItem(row)">删除</el-button>
          </div>
        </article>
      </section>

      <EmptyState v-if="loadError" title="加载失败" description="购物车数据加载失败，请检查网络后重试。">
        <el-button type="danger" @click="load">重新加载</el-button>
      </EmptyState>
      <EmptyState v-else-if="!loading && !items.length" title="购物车是空的" description="去商品列表挑选心仪的商品吧。">
        <el-button type="danger" @click="$router.push('/products')">去逛逛</el-button>
      </EmptyState>
      <div v-if="loading" class="loading-state">加载中...</div>

      <footer v-if="items.length" class="settlement">
        <div class="settlement-left">
          <span class="muted">已选 <b class="sel-count">{{ selected.length }}</b> 件</span>
          <span class="total-label">合计</span>
          <b class="total-price">&yen;{{ selectedTotal }}</b>
        </div>
        <el-button type="danger" size="large" :disabled="!selected.length" @click="checkout">去结算</el-button>
      </footer>
    </div>
  </ShopLayout>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useSessionStore } from '../store'
import ShopLayout from '../layouts/ShopLayout.vue'
import EmptyState from '../components/EmptyState.vue'

const router = useRouter()
const items = ref([])
const selected = ref([])
const loading = ref(true)
const loadError = ref(false)
const session = useSessionStore()
const tableRef = ref()
const selectedTotal = computed(() => selected.value.reduce((sum, i) => sum + Number(i.subtotal), 0).toFixed(2))

async function load() {
  loading.value = true
  loadError.value = false
  try {
    items.value = (await api.get('/cart', { params: { userId: session.userId } })).data.data || []
  } catch {
    loadError.value = true
    items.value = []
  } finally {
    loading.value = false
  }
}

async function updateQuantity(row, quantity) {
  await api.put('/cart/items', { cartItemId: row.id, productId: row.productId, specText: row.specText, quantity })
  load()
}

async function removeItem(row) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete('/cart/items', { params: { cartItemId: row.id, productId: row.productId, specText: row.specText } })
    ElMessage.success('已删除')
    load()
  } catch { /* user cancelled */ }
}

function changeSelection(rows) { selected.value = rows }
function selectAll() {
  selected.value = [...items.value]
  nextTick(() => {
    tableRef.value?.clearSelection()
    items.value.forEach(row => tableRef.value?.toggleRowSelection(row, true))
  })
}
function invertSelection() {
  const next = items.value.filter(row => !selected.value.includes(row))
  selected.value = next
  nextTick(() => {
    tableRef.value?.clearSelection()
    next.forEach(row => tableRef.value?.toggleRowSelection(row, true))
  })
}
function toggleMobile(row, checked) {
  selected.value = checked ? [...selected.value, row] : selected.value.filter(i => i !== row)
}
function checkout() {
  sessionStorage.setItem('checkoutCartItemIds', JSON.stringify(selected.value.map(i => i.id)))
  router.push('/checkout')
}
function imgFallback(e) {
  e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 80 80"><rect fill="%23f3f4f6" width="80" height="80"/><text x="40" y="40" text-anchor="middle" dy=".35em" fill="%239ca3af" font-size="10">暂无图片</text></svg>'
}

onMounted(load)
</script>

<style scoped>
.steps {
  padding: 20px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: var(--radius-lg);
}
.cart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}
h1, p { margin: 0; }
p { color: var(--muted); margin-top: 4px; }

.loading-state {
  text-align: center; padding: 60px 20px; color: var(--muted); font-size: 15px;
}

.bulk {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.cart-table :deep(th) { background: #f8fafc; }
.goods-cell { display: flex; gap: 12px; align-items: center; }
.goods-cell img { width: 72px; height: 72px; object-fit: contain; background: #f6f6f6; border: 1px solid #f1f5f9; border-radius: 6px; }
.goods-cell div { display: grid; gap: 6px; }
.goods-cell span { color: var(--muted); font-size: 13px; }

.settlement {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  position: sticky;
  bottom: 0;
  z-index: 2;
  padding: 16px 20px;
  margin-top: 20px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: 0 -6px 20px rgba(31, 41, 55, .08);
}
.settlement-left { display: flex; align-items: baseline; gap: 16px; }
.sel-count { color: var(--brand); font-size: 18px; }
.total-label { font-size: 15px; }
.total-price { color: var(--brand); font-size: 26px; font-weight: 800; }

.mobile-cart.mobile-only { display: none; gap: 12px; }
.mobile-item {
  display: grid;
  grid-template-columns: auto 86px 1fr;
  gap: 10px;
  padding: 14px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
}
.mobile-item img { width: 86px; height: 86px; object-fit: contain; background: #f6f6f6; border-radius: 6px; }
.mobile-item div { display: grid; gap: 8px; }
.mobile-price-qty { display: flex; justify-content: space-between; align-items: center; gap: 8px; }

@media (max-width: 768px) {
  .mobile-cart.mobile-only { display: grid; }
  .cart-head, .settlement { flex-direction: column; align-items: flex-start; }
  .settlement-left { flex-wrap: wrap; }
}
</style>
