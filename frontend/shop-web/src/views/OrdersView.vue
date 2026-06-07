<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchMyOrders, cancelOrder, confirmOrder, refundOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()
const orders = ref<any[]>([])
const activeStatus = ref('')
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusTabs = [
  { value: '', label: '全部' },
  { value: 'PENDING', label: '待支付' },
  { value: 'PAID', label: '待发货' },
  { value: 'SHIPPED', label: '待收货' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
]

async function load() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize.value }
    if (activeStatus.value) params.status = activeStatus.value
    const res: any = await fetchMyOrders(params)
    orders.value = res.data?.items ?? (Array.isArray(res.data) ? res.data : [])
    total.value = res.data?.total ?? orders.value.length
  } catch { orders.value = [] }
  finally { loading.value = false }
}

async function handleCancel(order: any) {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
    await cancelOrder(order.id)
    ElMessage.success('已取消')
    load()
  } catch { /* cancelled */ }
}

async function handleConfirm(order: any) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', { type: 'warning' })
    await confirmOrder(order.id)
    ElMessage.success('已确认收货')
    load()
  } catch { /* cancelled */ }
}

async function handleRefund(order: any) {
  try {
    await ElMessageBox.confirm('确认申请退款？', '提示', { type: 'warning' })
    await refundOrder(order.id)
    ElMessage.success('退款申请已提交')
    load()
  } catch { /* cancelled */ }
}

function viewDetail(id: number) { router.push(`/orders/${id}`) }

// 支持 ?status= 查询参数自动切换 Tab
watch(() => route.query.status, (s) => {
  const status = (s as string) || ''
  if (status !== activeStatus.value) {
    activeStatus.value = status
    currentPage.value = 1
    load()
  }
}, { immediate: true })

onMounted(() => {
  // 如果没有 query 参数，用默认空状态加载
  if (!route.query.status) load()
})
</script>

<template>
  <div class="page-container orders-page">
    <div class="page-intro">
      <div>
        <h2 class="page-title">我的订单</h2>
        <p>按状态查看订单，支持支付、取消、确认收货、退款和物流查看。</p>
      </div>
    </div>
    <div class="page-metrics">
      <div class="metric-card"><span>当前订单</span><strong>{{ orders.length }}</strong><small>筛选结果</small></div>
      <div class="metric-card"><span>当前状态</span><strong>{{ activeStatus || '全部' }}</strong><small>状态筛选</small></div>
      <div class="metric-card"><span>业务链路</span><strong>完整</strong><small>下单到售后</small></div>
    </div>

    <div class="status-tabs">
      <button v-for="t in statusTabs" :key="t.value"
              :class="['tab', { active: activeStatus === t.value }]"
              @click="activeStatus = t.value; currentPage = 1; load()">{{ t.label }}</button>
    </div>

    <div v-if="loading"><el-skeleton :rows="5" animated /></div>

    <div v-else-if="!orders.length" class="empty">
      <el-empty description="暂无订单">
        <el-button type="primary" @click="router.push('/products')">去购物</el-button>
      </el-empty>
    </div>

    <template v-else>
      <div v-for="o in orders" :key="o.id" class="order-card">
        <div class="order-header">
          <span class="order-no">{{ o.orderNo }}</span>
          <span class="order-status" :class="'status-'+o.status?.toLowerCase()">{{ o.status }}</span>
        </div>
        <div class="order-body" @click="viewDetail(o.id)">
          <div class="order-summary">共 {{ o.items?.length || '-' }} 件商品</div>
          <div class="order-amount">合计 ¥{{ o.totalAmount }}</div>
        </div>
        <div class="order-actions">
          <el-button v-if="o.status==='PENDING'" size="small" @click="handleCancel(o)">取消</el-button>
          <el-button v-if="o.status==='PENDING'" size="small" type="primary" @click="router.push(`/payment?orderNo=${o.orderNo}&id=${o.id}`)">去支付</el-button>
          <el-button v-if="o.status==='SHIPPED'" size="small" type="primary" @click="handleConfirm(o)">确认收货</el-button>
          <el-button v-if="o.status==='PAID'||o.status==='SHIPPED'" size="small" @click="handleRefund(o)">退款</el-button>
        </div>
      </div>
      <el-pagination v-if="total > pageSize" style="margin-top:16px;justify-content:center"
        :current-page="currentPage" :page-size="pageSize" :total="total"
        layout="prev, pager, next" @current-change="(p: number) => { currentPage = p; load() }" />
    </template>
  </div>
</template>

<style scoped>
.orders-page { max-width: 800px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 20px; }

.status-tabs { display: flex; gap: 0; margin-bottom: 20px; background: #f5f7fa; border-radius: 8px; overflow: hidden; }
.tab { flex: 1; padding: 10px 0; border: none; background: transparent; font-size: 13px; cursor: pointer; color: #666; transition: all 0.2s; }
.tab.active { background: var(--color-primary); color: #fff; font-weight: 600; }

.order-card { background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.order-header { display: flex; justify-content: space-between; margin-bottom: 10px; }
.order-no { font-size: 13px; color: #888; }
.order-status { font-size: 12px; padding: 2px 8px; border-radius: 4px; font-weight: 600; }
.status-pending { color: #f59e0b; background: #fef3c7; }
.status-paid { color: #3b82f6; background: #dbeafe; }
.status-shipped { color: #8b5cf6; background: #ede9fe; }
.status-completed { color: #22c55e; background: #dcfce7; }
.status-cancelled { color: #ef4444; background: #fee2e2; }

.order-body { display: flex; justify-content: space-between; padding: 10px 0; border-top:1px solid #f0f0f0; cursor: pointer; }
.order-amount { font-size: 16px; font-weight: 700; color: var(--color-price, #ff0036); }
.order-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 10px; }
</style>
