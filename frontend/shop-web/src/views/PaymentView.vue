<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { payOrder, fetchMyOrders, fetchOrderDetail } from '@/api/order'

const router = useRouter()
const route = useRoute()

const orderNo = ref((route.query.orderNo as string) || '')
const orderId = ref(route.query.id ? Number(route.query.id) : 0)
const orderInfo = ref<any>(null)
const paying = ref(false)

async function load() {
  if (!orderNo.value && !orderId.value) { router.replace('/orders'); return }
  try {
    if (orderId.value) {
      const res: any = await fetchOrderDetail(orderId.value)
      orderInfo.value = res.data ?? res
    } else {
      const res: any = await fetchMyOrders()
      const items = res.data?.items ?? (Array.isArray(res.data) ? res.data : [])
      orderInfo.value = items.find((o: any) => o.orderNo === orderNo.value)
    }
  } catch { /* handled */ }
}

async function handlePay() {
  if (!orderInfo.value) return
  paying.value = true
  try {
    await payOrder(orderInfo.value.id)
    ElMessage.success('支付成功')
    router.replace(`/orders/${orderInfo.value.id}`)
  } catch { /* handled */ }
  finally { paying.value = false }
}

onMounted(load)
</script>

<template>
  <div class="page-container pay-page">
    <div class="page-intro">
      <div>
        <h2 class="page-title">订单支付</h2>
        <p>模拟支付订单，支付成功后订单状态会进入后续履约流程。</p>
      </div>
    </div>

    <div v-if="!orderInfo" class="loading"><el-skeleton :rows="4" /></div>

    <template v-else>
      <div class="pay-card">
        <div class="pay-header">
          <span class="pay-icon">📱</span>
          <span class="pay-status">待支付</span>
        </div>
        <div class="pay-info">
          <div class="pay-row"><span>订单编号</span><span>{{ orderInfo.orderNo }}</span></div>
          <div class="pay-row"><span>支付金额</span><b>¥{{ orderInfo.totalAmount }}</b></div>
          <div class="pay-row"><span>支付方式</span><span>模拟支付</span></div>
        </div>
        <el-button type="primary" size="large" :loading="paying" @click="handlePay" style="width:100%;margin-top:20px">
          确认支付 ¥{{ orderInfo.totalAmount }}
        </el-button>
        <div class="pay-actions">
          <el-button @click="router.push('/orders')">返回订单</el-button>
          <el-button @click="router.push('/products')">继续购物</el-button>
        </div>
        <p class="pay-hint">本平台使用模拟支付，点击即完成付款</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.pay-page { max-width: 500px; padding-top: 40px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; text-align: center; }
.pay-card { background: #fff; border-radius: 16px; padding: 32px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }
.pay-header { text-align: center; margin-bottom: 24px; }
.pay-icon { font-size: 48px; }
.pay-status { display: block; font-size: 18px; font-weight: 600; margin-top: 8px; color: #f59e0b; }
.pay-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px; color: #666; }
.pay-row b { color: var(--color-price, #ff0036); font-size: 18px; }
.pay-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 12px; }
.pay-hint { text-align: center; color: #bbb; font-size: 12px; margin-top: 12px; }
</style>
