<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { fetchOrderDetail } from '@/api/order'

const route = useRoute()
const order = ref<any>(null)
const items = ref<any[]>([])
const address = ref<any>(null)
const logistics = ref<any[]>([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res: any = await fetchOrderDetail(id)
    const data = res.data ?? res
    order.value = data.order ?? data
    items.value = data.items ?? data.orderItems ?? []
    address.value = data.address
    logistics.value = data.logistics ?? []
  } catch { /* handled */ }
  finally { loading.value = false }
}

onMounted(load)
</script>

<template>
  <div class="page-container detail-page">
    <h2 class="page-title">订单详情</h2>

    <div v-if="loading"><el-skeleton :rows="8" animated /></div>

    <template v-else-if="order">
      <!-- 状态 -->
      <div class="detail-card">
        <div class="d-row"><span>订单编号</span><span>{{ order.orderNo }}</span></div>
        <div class="d-row"><span>订单状态</span><span class="order-status">{{ order.status }}</span></div>
        <div class="d-row"><span>支付方式</span><span>{{ order.paymentMethod || '模拟支付' }}</span></div>
        <div class="d-row"><span>下单时间</span><span>{{ order.createdAt }}</span></div>
      </div>

      <!-- 物流 -->
      <div v-if="logistics.length" class="detail-card">
        <h3 class="card-head">物流信息</h3>
        <div class="log-item" v-for="l in logistics" :key="l.id">
          <span class="log-content">{{ l.content }}</span>
          <span class="log-time">{{ l.createdAt }}</span>
        </div>
      </div>

      <!-- 地址 -->
      <div v-if="address" class="detail-card">
        <h3 class="card-head">收货地址</h3>
        <p>{{ address.receiver || address.receiverName }} {{ address.phone }}</p>
        <p>{{ address.province }}{{ address.city }}{{ address.district }} {{ address.detail || address.detailAddress }}</p>
      </div>

      <!-- 商品 -->
      <div class="detail-card">
        <h3 class="card-head">商品清单</h3>
        <div v-for="item in items" :key="item.id" class="detail-item">
          <span>{{ item.productName }}</span>
          <span>{{ item.specText || '' }}</span>
          <span>¥{{ item.unitPrice || item.price }} × {{ item.quantity }}</span>
        </div>
      </div>

      <div class="detail-total">合计：<b>¥{{ order.totalAmount }}</b></div>
    </template>
  </div>
</template>

<style scoped>
.detail-page { max-width: 700px; padding-bottom: 60px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; }

.detail-card { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.card-head { font-size: 15px; font-weight: 600; margin-bottom: 12px; padding-left: 8px; border-left: 3px solid var(--color-primary); }
.d-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px; color: #666; }
.d-row .order-status { color: var(--color-primary); font-weight: 600; }

.log-item { display: flex; justify-content: space-between; padding: 8px 0; font-size: 13px; }
.log-time { color: #bbb; font-size: 12px; }

.detail-item { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
.detail-item :last-child { margin-left: auto; font-weight: 600; }

.detail-total { text-align: right; font-size: 16px; padding: 16px 20px; }
.detail-total b { font-size: 24px; color: var(--color-price, #ff0036); }
</style>
