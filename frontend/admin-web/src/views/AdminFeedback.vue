<template>
  <AdminLayout>
    <AdminPageHeader title="反馈管理" eyebrow="Feedback" subtitle="查看、回复和处理用户提交的反馈信息。">
      <el-button type="danger" @click="load">刷新</el-button>
    </AdminPageHeader>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="type" label="类型" width="110">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="反馈内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系方式" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusTag :value="row.status" kind="order" /></template>
        </el-table-column>
        <el-table-column label="回复" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.reply" placeholder="输入回复内容" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="danger" size="small" @click="reply(row)">回复</el-button>
              <el-button link size="small" @click="processed(row.id)">标记已处理</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无反馈数据" description="当前没有用户提交的反馈信息。" />
      <div v-if="total > size" class="pager">
        <el-pagination layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="changePage" />
      </div>
    </section>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import EmptyState from '../components/EmptyState.vue'

const items = ref([]), page = ref(1), size = 10, total = ref(0)
async function load() {
  const result = (await api.get('/admin/feedback', { params: { page: page.value, size } })).data.data
  items.value = result.items || []; total.value = result.total || 0
}
async function reply(row) { await api.put('/admin/feedback', row); ElMessage.success('已回复'); load() }
async function processed(id) { await api.put(`/admin/feedback/${id}/processed`); ElMessage.success('已标记为已处理'); load() }
function changePage(v) { page.value = v; load() }
function typeLabel(type) {
  return ({ product: '商品问题', order: '订单问题', logistics: '物流问题', aftersale: '售后问题', account: '账户问题', other: '其他建议' })[type] || type || '-'
}
onMounted(load)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
