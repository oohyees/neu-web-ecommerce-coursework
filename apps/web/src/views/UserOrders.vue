<template>
  <section>
    <div class="page-head">
      <h1>我的订单</h1>
      <el-button @click="$router.push('/products')">继续购物</el-button>
    </div>

    <el-tabs v-model="filterKey" class="order-tabs" @tab-change="load">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="UNPAID" />
      <el-tab-pane label="待发货" name="PAID" />
      <el-tab-pane label="待收货" name="SHIPPED" />
      <el-tab-pane label="已完成" name="COMPLETED" />
      <el-tab-pane label="已取消" name="CANCELLED" />
    </el-tabs>

    <section v-if="orders.length" class="order-list">
      <article v-for="order in orders" :key="order.id" class="order-card">
        <header>
          <div>
            <strong>订单号 {{ order.orderNo }}</strong>
            <span class="muted">{{ order.createdAt }}</span>
          </div>
          <StatusTag :value="order.status" />
        </header>
        <main>
          <div class="amount-info">
            <span class="muted">实付金额</span>
            <b>&yen;{{ order.totalAmount }}</b>
          </div>
          <div class="status-line">
            <StatusTag :value="order.paymentStatus" kind="payment" />
            <span class="muted">物流：{{ order.logisticsStatus || '-' }}</span>
            <span class="muted">退款：{{ order.refundStatus || '-' }}</span>
          </div>
        </main>
        <footer>
          <el-button v-if="order.paymentStatus==='UNPAID'" type="danger" size="small" @click="$router.push(`/pay/${order.id}`)">立即支付</el-button>
          <el-button v-if="order.status==='CREATED'" size="small" @click="cancelOrder(order.id)">取消订单</el-button>
          <el-button v-if="order.status==='SHIPPED'" type="danger" size="small" @click="confirm(order.id)">确认收货</el-button>
          <el-button v-if="order.paymentStatus==='PAID'" size="small" @click="refund(order.id)">申请退款</el-button>
          <el-button size="small" @click="showLogistics(order)">物流轨迹</el-button>
          <el-button size="small" @click="openDetail(order)">订单详情</el-button>
        </footer>
      </article>
    </section>
    <EmptyState v-else title="暂无订单" description="完成下单后可以在这里查看订单状态。">
      <el-button type="danger" @click="$router.push('/products')">去购物</el-button>
    </EmptyState>

    <el-dialog v-model="detailVisible" title="订单详情" width="760px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="订单号">{{ detail.order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="金额">&yen;{{ detail.order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail.order.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.order.status }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" v-if="detail.address">{{ detail.address.province }}{{ detail.address.city }}{{ detail.address.district }}{{ detail.address.detailAddress }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="detail" :data="detail.items" style="margin-top:14px">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="specText" label="规格" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="subtotal" label="小计" width="120" />
      </el-table>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useSessionStore } from '../store'
import StatusTag from '../components/StatusTag.vue'
import EmptyState from '../components/EmptyState.vue'

const session = useSessionStore()
const orders = ref([])
const filterKey = ref('')
const detail = ref(null)
const detailVisible = ref(false)

async function load() {
  const params = { userId: session.userId }
  if (filterKey.value === 'UNPAID') params.paymentStatus = 'UNPAID'
  else if (filterKey.value === 'PAID') params.status = 'PAID'
  else if (filterKey.value) params.status = filterKey.value
  orders.value = (await api.get('/orders', { params })).data.data || []
}

async function cancelOrder(id) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', { confirmButtonText: '确定', cancelButtonText: '返回', type: 'warning' })
    await api.put(`/orders/${id}/cancel`)
    ElMessage.success('订单已取消')
    load()
  } catch { /* user cancelled */ }
}
async function confirm(id) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { confirmButtonText: '确认收货', cancelButtonText: '取消', type: 'warning' })
    await api.put(`/orders/${id}/confirm`)
    ElMessage.success('已确认收货')
    load()
  } catch { /* user cancelled */ }
}
async function refund(id) { await api.put(`/orders/${id}/refund`); ElMessage.success('退款申请已提交'); load() }
async function showLogistics(row) {
  const items = (await api.get(`/orders/${row.id}/logistics`)).data.data
  ElMessageBox.alert(items.map(i => `${i.createdAt} ${i.content}`).join('\n') || '暂无物流信息', '物流轨迹')
}
async function openDetail(row) {
  detail.value = (await api.get(`/orders/${row.id}`)).data.data
  detailVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  align-items: center;
}
h1 { margin: 0; font-size: 22px; }

.order-tabs {
  padding: 0 16px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: var(--radius-lg);
}

.order-list { display: grid; gap: 14px; }

.order-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.order-card header,
.order-card main,
.order-card footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 18px;
}
.order-card header {
  background: #f8fafc;
  border-bottom: 1px solid var(--line);
}
.order-card header div { display: grid; gap: 4px; }
.order-card header strong { font-size: 15px; }
.order-card header span,
.status-line span { font-size: 13px; color: var(--muted); }

.amount-info { display: grid; gap: 4px; }
.amount-info b { color: var(--brand); font-size: 24px; font-weight: 800; }

.status-line { display: flex; gap: 16px; align-items: center; }

.order-card footer {
  flex-wrap: wrap;
  border-top: 1px solid #f1f5f9;
}

@media (max-width: 768px) {
  .order-card header, .order-card main, .order-card footer {
    align-items: flex-start; flex-direction: column;
  }
}
</style>
