<template>
  <ShopLayout>
    <div class="page-wrap">
      <el-steps :active="1" finish-status="success" class="steps">
        <el-step title="购物车" />
        <el-step title="确认订单" />
        <el-step title="支付完成" />
      </el-steps>

      <section class="checkout-grid">
        <main>
          <section class="page-card block">
            <div class="block-head">
              <h2>收货地址</h2>
              <el-button link type="danger" @click="$router.push('/user/addresses')">管理地址</el-button>
            </div>
            <div v-if="addresses.length" class="address-grid">
              <article v-for="a in addresses" :key="a.id" :class="['address-card', { active: addressId === a.id }]" @click="addressId = a.id">
                <strong>{{ a.receiverName }} {{ a.phone }}</strong>
                <p>{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detailAddress }}</p>
                <el-tag v-if="a.isDefault" type="danger" size="small">默认</el-tag>
              </article>
            </div>
            <EmptyState v-else title="暂无收货地址" description="请先新增地址后提交订单。">
              <el-button type="danger" @click="$router.push('/user/addresses')">新增地址</el-button>
            </EmptyState>
          </section>

          <section class="page-card block">
            <h2>商品清单</h2>
            <el-table :data="items" class="goods-table">
              <el-table-column label="商品" min-width="260">
                <template #default="{ row }">
                  <div class="goods-cell">
                    <img :src="row.imageUrl" @error="imgFallback" />
                    <div><strong>{{ row.productName }}</strong><span class="muted">{{ row.specText || '默认规格' }}</span></div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="110">
                <template #default="{ row }"><span class="price">&yen;{{ row.price }}</span></template>
              </el-table-column>
              <el-table-column prop="quantity" label="数量" width="90" />
              <el-table-column label="小计" width="120">
                <template #default="{ row }"><span class="price">&yen;{{ row.subtotal }}</span></template>
              </el-table-column>
            </el-table>
          </section>

          <section class="page-card block">
            <h2>优惠与支付</h2>
            <div class="pay-options">
              <el-select v-model="couponId" placeholder="选择我的优惠券" clearable style="width:260px">
                <el-option v-for="c in coupons" :key="c.id" :label="`${c.name}（满${c.thresholdAmount}减${c.discountAmount}）`" :value="c.id" />
              </el-select>
              <el-radio-group v-model="paymentMethod">
                <el-radio-button label="模拟支付" value="MOCK_PAY">模拟支付</el-radio-button>
                <el-radio-button label="货到付款" value="CASH_ON_DELIVERY">货到付款</el-radio-button>
              </el-radio-group>
            </div>
          </section>
        </main>

        <aside class="summary">
          <MoneySummary :total="total" :discount="discount" :payable="Number(payable)" />
          <el-button type="danger" size="large" :disabled="!items.length || !addressId" @click="submit">提交订单</el-button>
        </aside>
      </section>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useUserStore, useCartStore, useFavoriteStore } from '@/stores'
import ShopLayout from '../layouts/ShopLayout.vue'
import EmptyState from '../components/EmptyState.vue'
import MoneySummary from '../components/MoneySummary.vue'

const userStore = useUserStore()
const router = useRouter()
const items = ref<any[]>([])
const addresses = ref<any[]>([])
const coupons = ref<any[]>([])
const addressId = ref(null)
const couponId = ref(null)
const paymentMethod = ref('MOCK_PAY')
const total = computed(() => items.value.reduce((s, i) => s + Number(i.subtotal), 0))
const activeCoupon = computed(() => coupons.value.find(c => c.id === couponId.value))
const discount = computed(() => activeCoupon.value && total.value >= activeCoupon.value.thresholdAmount ? Number(activeCoupon.value.discountAmount) : 0)
const payable = computed(() => Math.max(0, total.value - discount.value).toFixed(2))

async function load() {
  const selectedCartItemIds = JSON.parse(sessionStorage.getItem('checkoutCartItemIds') || '[]')
  const selectedProductIds = JSON.parse(sessionStorage.getItem('checkoutProductIds') || '[]')
  items.value = (await api.get('/cart')).data.data.filter(i => {
    if (selectedCartItemIds.length) return selectedCartItemIds.includes(i.id)
    return !selectedProductIds.length || selectedProductIds.includes(i.productId)
  })
  addresses.value = (await api.get('/addresses', { params: { userId: userStore.userId } })).data.data || []
  coupons.value = (await api.get(`/marketing/coupons/user/${userStore.userId}`)).data.data || []
  const defaultAddress = addresses.value.find(a => a.isDefault) || addresses.value[0]
  if (defaultAddress) addressId.value = defaultAddress.id
}
async function submit() {
  const cartItemIds = JSON.parse(sessionStorage.getItem('checkoutCartItemIds') || '[]')
  const productIds = JSON.parse(sessionStorage.getItem('checkoutProductIds') || '[]')
  const { data } = await api.post('/orders', { addressId: addressId.value, cartItemIds, productIds, couponId: couponId.value, paymentMethod: paymentMethod.value })
  if (!data.success) return ElMessage.error(data.message)
  sessionStorage.removeItem('checkoutCartItemIds')
  sessionStorage.removeItem('checkoutProductIds')
  ElMessage.success(`下单成功：${data.data.orderNo}`)
  router.push('/orders')
}
function imgFallback(e: Event) {
  (e.target as HTMLImageElement).src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><rect fill="%23f3f4f6" width="64" height="64"/><text x="32" y="32" text-anchor="middle" dy=".35em" fill="%239ca3af" font-size="8">无图</text></svg>'
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
.checkout-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
}
.checkout-grid > main {
  min-width: 0;
}
.block {
  margin-bottom: 16px;
  min-width: 0;
  overflow-x: auto;
}
.block h2 {
  margin: 0 0 14px;
  font-size: 18px;
  font-weight: 700;
}
.block-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.block-head h2 { margin: 0; }

.address-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.address-card {
  min-height: 112px;
  padding: 14px;
  cursor: pointer;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  transition: border-color .2s;
}
.address-card.active {
  border-color: var(--brand);
  box-shadow: inset 0 0 0 1px var(--brand);
}
.address-card:hover { border-color: var(--brand); }
.address-card p { color: var(--muted); line-height: 1.6; font-size: 14px; margin: 6px 0; }
.address-card strong { font-size: 15px; }

.goods-cell { display: flex; gap: 12px; align-items: center; }
.goods-cell img { width: 60px; height: 60px; object-fit: contain; background: #f6f6f6; border-radius: 6px; }
.goods-cell div { display: grid; gap: 4px; }
.goods-cell strong { font-size: 14px; }

.pay-options {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 16px;
  align-items: center;
}
.pay-options :deep(.el-select) {
  width: 100% !important;
  max-width: 100%;
}

.summary {
  position: sticky;
  top: 16px;
  display: grid;
  align-self: start;
  gap: 14px;
}

@media (max-width: 900px) {
  .checkout-grid, .address-grid, .pay-options {
    grid-template-columns: 1fr;
  }
}
</style>
