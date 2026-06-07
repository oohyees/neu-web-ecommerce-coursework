<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Search } from '@element-plus/icons-vue'
import { fetchAdminOrders, shipOrder, approveRefund, updateOrderStatus } from '@/api/order'
import { exportFile } from '@/utils/export'

const orders = ref<any[]>([]); const total = ref(0); const page = ref(1); const keyword = ref(''); const status = ref(''); const loading = ref(false)
const exportFormat = ref<'xlsx' | 'csv'>('xlsx')

async function load() {
  loading.value = true
  try { const params: any = { page: page.value, size: 10 }; if (keyword.value) params.keyword = keyword.value; if (status.value) params.status = status.value
    const res: any = await fetchAdminOrders(params); orders.value = res.data?.items ?? []; total.value = res.data?.total ?? 0 } catch { orders.value = [] }
  finally { loading.value = false }
}
async function handleShip(o: any) { try { await shipOrder(o.id); ElMessage.success('已发货'); load() } catch { /* handled */ } }
async function handleRefund(o: any) { try { await approveRefund(o.id); ElMessage.success('已同意退款'); load() } catch { /* handled */ } }
async function handleCancel(o: any) { try { await updateOrderStatus(o.id, 'CANCELLED'); ElMessage.success('已取消'); load() } catch { /* handled */ } }
function applySearch() { page.value = 1; load() }
function resetFilters() { keyword.value = ''; status.value = ''; page.value = 1; load() }
const statusText: Record<string, string> = { PENDING: '待支付', PAID: '待发货', SHIPPED: '待收货', COMPLETED: '已完成', CANCELLED: '已取消', REFUND_REQUESTED: '退款中' }
const paymentText: Record<string, string> = { PAID: '已支付', UNPAID: '未支付', REFUNDED: '已退款' }
function statusClass(v: string) {
  if (v === 'COMPLETED' || v === 'SHIPPED') return 'status-pill--success'
  if (v === 'PENDING' || v === 'PAID' || v === 'REFUND_REQUESTED') return 'status-pill--warning'
  if (v === 'CANCELLED') return 'status-pill--danger'
  return 'status-pill--info'
}
const paidCount = computed(() => orders.value.filter((o) => o.paymentStatus === 'PAID').length)
const shipCount = computed(() => orders.value.filter((o) => o.status === 'PAID').length)
const currentPageAmount = computed(() => orders.value.reduce((sum, o) => sum + Number(o.totalAmount || 0), 0))

function handleExport() {
  if (!orders.value.length) { ElMessage.warning('当前无数据可导出'); return }
  const columns = [
    { key: 'orderNo', label: '订单号' },
    { key: 'userId', label: '用户ID' },
    { key: 'totalAmount', label: '金额' },
    { key: 'status', label: '订单状态' },
    { key: 'paymentStatus', label: '支付状态' },
    { key: 'createTime', label: '创建时间' },
  ]
  const data = orders.value.map((o) => ({
    ...o,
    status: statusText[o.status] || o.status,
    paymentStatus: paymentText[o.paymentStatus] || o.paymentStatus,
  }))
  exportFile(data, columns, `订单导出_${new Date().toISOString().slice(0, 10)}`, exportFormat.value)
  ElMessage.success(`已导出 ${data.length} 条记录（${exportFormat.value.toUpperCase()}）`)
}

onMounted(load)
</script>
<template>
  <div class="admin-page orders-page">
    <div class="tb-header">
      <div>
        <h2>订单管理</h2>
        <p class="page-subtitle">跟踪支付、发货、退款和取消状态，保证用户端订单状态能和后台同步。</p>
      </div>
      <div class="tb-actions">
        <el-input v-model="keyword" :prefix-icon="Search" placeholder="订单号/用户" size="default" style="width:220px" clearable @keyup.enter="applySearch" @clear="applySearch" />
        <el-select v-model="status" placeholder="状态" size="default" style="width:120px" clearable @change="load">
          <el-option label="待支付" value="PENDING" /><el-option label="已支付" value="PAID" /><el-option label="已发货" value="SHIPPED" /><el-option label="已完成" value="COMPLETED" /><el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
        <el-select v-model="exportFormat" size="default" style="width:100px">
          <el-option label="XLSX" value="xlsx" />
          <el-option label="CSV" value="csv" />
        </el-select>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
      </div>
    </div>

    <div class="admin-summary">
      <div class="summary-card"><div class="summary-card__label">筛选总记录</div><div class="summary-card__value">{{ total }}</div><div class="summary-card__hint">符合当前筛选条件</div></div>
      <div class="summary-card"><div class="summary-card__label">本页金额</div><div class="summary-card__value">¥{{ currentPageAmount.toFixed(0) }}</div><div class="summary-card__hint">当前页实时统计</div></div>
      <div class="summary-card"><div class="summary-card__label">本页已支付</div><div class="summary-card__value">{{ paidCount }}</div><div class="summary-card__hint">可进入履约流程</div></div>
      <div class="summary-card"><div class="summary-card__label">本页待发货</div><div class="summary-card__value">{{ shipCount }}</div><div class="summary-card__hint">后台最常用操作</div></div>
    </div>

    <div class="table-panel">
      <el-table :data="orders" stripe v-loading="loading">
        <el-table-column label="订单" min-width="220">
          <template #default="{row}">
            <div class="order-cell">
              <strong>{{ row.orderNo }}</strong>
              <span>用户 ID {{ row.userId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120"><template #default="{row}"><span class="money">¥{{ Number(row.totalAmount || 0).toFixed(2) }}</span></template></el-table-column>
        <el-table-column label="订单状态" width="120"><template #default="{row}"><span :class="['status-pill', statusClass(row.status)]">{{ statusText[row.status] || row.status }}</span></template></el-table-column>
        <el-table-column label="支付" width="110"><template #default="{row}"><span :class="['status-pill', row.paymentStatus==='PAID'?'status-pill--success':'status-pill--warning']">{{ paymentText[row.paymentStatus] || row.paymentStatus }}</span></template></el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <div class="action-stack">
              <el-button v-if="row.status==='PAID'" size="small" type="primary" @click="handleShip(row)">发货</el-button>
              <el-button v-if="row.refundStatus==='REQUESTED'" size="small" type="warning" @click="handleRefund(row)">同意退款</el-button>
              <el-button v-if="row.status==='PENDING'" size="small" type="danger" @click="handleCancel(row)">取消</el-button>
              <span v-if="row.status!=='PAID' && row.refundStatus!=='REQUESTED' && row.status!=='PENDING'" class="muted">暂无操作</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-panel__footer">
        <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @change="load" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }
.order-cell { display: grid; gap: 5px; }
.order-cell strong { color: #1f2937; font-size: 14px; }
.order-cell span, .muted { color: #8a94a6; font-size: 12px; }
</style>
