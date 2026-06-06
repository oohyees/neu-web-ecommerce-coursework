<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminOrders, shipOrder, approveRefund, updateOrderStatus, exportOrders } from '@/api/order'

const orders = ref<any[]>([]); const total = ref(0); const page = ref(1); const keyword = ref(''); const status = ref(''); const loading = ref(false)

async function load() {
  loading.value = true
  try { const params: any = { page: page.value, size: 10 }; if (keyword.value) params.keyword = keyword.value; if (status.value) params.status = status.value
    const res: any = await fetchAdminOrders(params); orders.value = res.data?.items ?? []; total.value = res.data?.total ?? 0 } catch { orders.value = [] }
  finally { loading.value = false }
}
async function handleShip(o: any) { try { await shipOrder(o.id); ElMessage.success('已发货'); load() } catch { /* handled */ } }
async function handleRefund(o: any) { try { await approveRefund(o.id); ElMessage.success('已同意退款'); load() } catch { /* handled */ } }
async function handleCancel(o: any) { try { await updateOrderStatus(o.id, 'CANCELLED'); ElMessage.success('已取消'); load() } catch { /* handled */ } }
onMounted(load)
</script>
<template>
  <div>
    <div class="tb-header">
      <h2>订单管理</h2>
      <div class="tb-actions">
        <el-input v-model="keyword" placeholder="订单号/用户" size="default" style="width:180px" clearable @change="load" />
        <el-select v-model="status" placeholder="状态" size="default" style="width:120px" clearable @change="load">
          <el-option label="待支付" value="PENDING" /><el-option label="已支付" value="PAID" /><el-option label="已发货" value="SHIPPED" /><el-option label="已完成" value="COMPLETED" /><el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button @click="exportOrders()">导出Excel</el-button>
      </div>
    </div>
    <el-table :data="orders" stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="140" />
      <el-table-column prop="userId" label="用户ID" width="70" />
      <el-table-column prop="totalAmount" label="金额" width="90" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag size="small">{{row.status}}</el-tag></template></el-table-column>
      <el-table-column label="支付" width="80"><template #default="{row}"><el-tag :type="row.paymentStatus==='PAID'?'success':'warning'" size="small">{{row.paymentStatus}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button v-if="row.status==='PAID'" text size="small" type="primary" @click="handleShip(row)">发货</el-button>
          <el-button v-if="row.refundStatus==='REQUESTED'" text size="small" type="warning" @click="handleRefund(row)">同意退款</el-button>
          <el-button v-if="row.status==='PENDING'" text size="small" type="danger" @click="handleCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
  </div>
</template>
<style scoped>
.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.tb-header h2 { font-size: 20px; font-weight: 700; }
.tb-actions { display: flex; gap: 8px; }
</style>
