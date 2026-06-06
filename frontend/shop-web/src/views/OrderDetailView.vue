<template>
  <ShopLayout>
    <div class="page-wrap">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/user/orders' }">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>

      <div v-if="loading" class="loading-state">加载中...</div>

      <template v-else-if="order">
        <section class="page-card order-header">
          <div>
            <h1>订单号：{{ order.orderNo }}</h1>
            <p class="muted">{{ order.createdAt }}</p>
          </div>
          <StatusTag :value="order.status" />
        </section>

        <section class="page-card">
          <h2>收货地址</h2>
          <div v-if="order.address" class="address-info">
            <strong>{{ order.address.receiverName }} {{ order.address.phone }}</strong>
            <p>{{ order.address.province }}{{ order.address.city }}{{ order.address.district }}{{ order.address.detail }}</p>
          </div>
        </section>

        <section class="page-card">
          <h2>商品清单</h2>
          <el-table :data="order.items">
            <el-table-column label="商品" min-width="240">
              <template #default="{ row }">
                <div class="goods-cell">
                  <img :src="row.productImage" @error="imgFallback" />
                  <span>{{ row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="单价" width="120">
              <template #default="{ row }"><span class="price">&yen;{{ row.price }}</span></template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="小计" width="120">
              <template #default="{ row }"><span class="price">&yen;{{ (row.price * row.quantity).toFixed(2) }}</span></template>
            </el-table-column>
          </el-table>
        </section>

        <section class="page-card order-footer">
          <div class="summary-row">
            <span>商品总额</span>
            <b>&yen;{{ order.totalAmount }}</b>
          </div>
          <div class="summary-row">
            <span>支付方式</span>
            <span>{{ order.paymentMethod || '未选择' }}</span>
          </div>
          <el-divider />
          <div class="summary-row total">
            <span>实付金额</span>
            <b class="price">&yen;{{ order.totalAmount }}</b>
          </div>

          <div class="actions" style="margin-top:16px">
            <el-button v-if="order.status === 'PENDING_PAYMENT'" type="danger" size="large" @click="$router.push(`/pay/${order.id}`)">立即支付</el-button>
            <el-button v-if="order.status === 'PENDING_RECEIPT'" type="danger" size="large" @click="confirmReceipt">确认收货</el-button>
          </div>
        </section>
      </template>

      <EmptyState v-else title="订单不存在" description="该订单信息未找到。">
        <el-button type="danger" @click="$router.push('/user/orders')">返回订单列表</el-button>
      </EmptyState>
    </div>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'
import type { Order } from '@/types'
import ShopLayout from '@/layouts/ShopLayout.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const order = ref<Order | null>(null)
const loading = ref(true)

async function confirmReceipt() {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { confirmButtonText: '确认收货', cancelButtonText: '取消', type: 'warning' })
    await api.put(`/orders/${route.params.id}/confirm`)
    ElMessage.success('已确认收货')
    order.value!.status = 'COMPLETED'
  } catch { /* cancelled */ }
}

function imgFallback(e: Event) {
  (e.target as HTMLImageElement).src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><rect fill="%23f3f4f6" width="64" height="64"/><text x="32" y="32" text-anchor="middle" dy=".35em" fill="%239ca3af" font-size="8">无图</text></svg>'
}

onMounted(async () => {
  try {
    const res = await api.get(`/orders/${route.params.id}`)
    order.value = res.data.data?.order || res.data.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.breadcrumb { margin-bottom: 16px; }
.loading-state { text-align: center; padding: 60px; color: var(--muted); }

.order-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 16px; }
.order-header h1 { font-size: 18px; margin: 0 0 4px; }

.address-info { line-height: 1.6; }
.address-info strong { font-size: 15px; }
.address-info p { color: var(--muted); margin: 4px 0 0; }

.goods-cell { display: flex; gap: 12px; align-items: center; }
.goods-cell img { width: 56px; height: 56px; object-fit: contain; background: #f6f6f6; border-radius: 6px; }

.order-footer { margin-top: 16px; }
.summary-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; }
.summary-row b { font-size: 18px; }
.total { font-size: 16px; }
.actions { display: flex; gap: 12px; }

@media (max-width: 768px) {
  .order-header { flex-direction: column; align-items: flex-start; }
}
</style>
