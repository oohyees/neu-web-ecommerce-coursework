<template>
  <AdminLayout>
    <AdminPageHeader title="订单管理" eyebrow="Order" subtitle="处理订单查询、发货、退款、状态调整和订单导出。">
      <el-button @click="download">导出订单</el-button>
    </AdminPageHeader>

    <section class="toolbar-panel">
      <el-input v-model="keyword" placeholder="订单号 / 用户" style="width:240px" clearable @keyup.enter="load" />
      <el-select v-model="status" placeholder="订单状态" clearable style="width:160px" @change="load">
        <el-option label="待处理" value="CREATED" />
        <el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="danger" @click="load">搜索</el-button>
    </section>

    <section class="admin-card">
      <el-table v-if="orders.length" :data="orders">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column label="金额" width="120"><template #default="{ row }"><span class="price">&yen;{{ row.totalAmount }}</span></template></el-table-column>
        <el-table-column label="订单状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column label="支付" width="110"><template #default="{ row }"><StatusTag :value="row.paymentStatus" kind="payment" /></template></el-table-column>
        <el-table-column prop="logisticsStatus" label="物流" width="100" />
        <el-table-column prop="refundStatus" label="退款" width="100" />
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="row.status==='CREATED'" link type="danger" size="small" @click="ship(row.id)">发货</el-button>
              <el-button v-if="row.refundStatus==='REQUESTED'" link size="small" @click="approveRefund(row.id)">同意退款</el-button>
              <el-button link size="small" @click="openDetail(row)">详情</el-button>
              <el-dropdown @command="cmd => updateStatus(row.id, cmd)">
                <span class="link">改状态</span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="CREATED">待处理</el-dropdown-item>
                    <el-dropdown-item command="SHIPPED">已发货</el-dropdown-item>
                    <el-dropdown-item command="COMPLETED">已完成</el-dropdown-item>
                    <el-dropdown-item command="CANCELLED">已取消</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无订单数据" description="当前没有符合条件的订单。" />
      <div v-if="total > size" class="pager"><el-pagination layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="changePage" /></div>
    </section>

    <el-dialog v-model="detailVisible" title="订单详情" width="780px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="订单号">{{ detail.order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.order.userId }}</el-descriptions-item>
        <el-descriptions-item label="金额">&yen;{{ detail.order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="优惠">&yen;{{ detail.order.discountAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail.order.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" v-if="detail.address">{{ detail.address.province }}{{ detail.address.city }}{{ detail.address.district }}{{ detail.address.detailAddress }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.order.status }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="detail" :data="detail.items" style="margin-top:14px">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="specText" label="规格" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="subtotal" label="小计" width="120" />
      </el-table>
    </el-dialog>
  </AdminLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import EmptyState from '../components/EmptyState.vue'

const orders = ref([]), keyword = ref(''), status = ref(''), detail = ref(null), detailVisible = ref(false)
const page = ref(1), size = 10, total = ref(0)

async function load() {
  const result = (await api.get('/admin/orders', { params: { keyword: keyword.value, status: status.value, page: page.value, size } })).data.data
  orders.value = result.items || []; total.value = result.total || 0
}
async function ship(id) { await api.put(`/admin/orders/${id}/ship`); ElMessage.success('已发货'); load() }
async function approveRefund(id) { await api.put(`/admin/orders/${id}/refund/approve`); ElMessage.success('退款已处理'); load() }
async function updateStatus(id, status) { await api.put(`/admin/orders/${id}/status`, null, { params: { status } }); ElMessage.success('状态已更新'); load() }
async function openDetail(row) { detail.value = (await api.get(`/orders/${row.id}`)).data.data; detailVisible.value = true }
function changePage(v) { page.value = v; load() }
async function download() {
  const response = await api.get('/admin/orders/export', { responseType: 'blob' })
  const url = URL.createObjectURL(response.data); const a = document.createElement('a')
  a.href = url; a.download = 'orders.xlsx'; a.click(); URL.revokeObjectURL(url)
}
onMounted(load)
</script>

<style scoped>
.link { color: var(--brand); cursor: pointer; font-size: 13px; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
