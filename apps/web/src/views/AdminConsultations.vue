<template>
  <AdminLayout>
    <AdminPageHeader title="客服咨询" eyebrow="Consultation" subtitle="回复和处理用户的客服咨询信息。">
      <el-button type="danger" @click="load">刷新</el-button>
    </AdminPageHeader>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="subject" label="主题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusTag :value="row.status" kind="order" /></template>
        </el-table-column>
        <el-table-column label="回复" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.reply" placeholder="输入回复" size="small" />
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
      <EmptyState v-else title="暂无咨询数据" description="当前没有用户的客服咨询信息。" />
      <div v-if="total > size" class="pager">
        <el-pagination layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="change" />
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
  const r = (await api.get('/admin/consultations', { params: { page: page.value, size } })).data.data
  items.value = r.items || []; total.value = r.total || 0
}
async function reply(row) { await api.put('/admin/consultations', row); ElMessage.success('已回复'); load() }
async function processed(id) { await api.put(`/admin/consultations/${id}/processed`); ElMessage.success('已标记为已处理'); load() }
function change(v) { page.value = v; load() }
onMounted(load)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
