<template>
  <AdminLayout>
    <AdminPageHeader title="用户管理" eyebrow="Users" subtitle="查看用户信息、启用或禁用账户。">
      <el-button type="danger" @click="load">刷新</el-button>
    </AdminPageHeader>

    <section class="toolbar-panel">
      <el-input v-model="keyword" placeholder="搜索账号/昵称/手机号" style="width:260px" clearable @keyup.enter="load" />
      <el-button type="danger" @click="load">搜索</el-button>
    </section>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link size="small" @click="openDetail(row)">详情</el-button>
              <el-button link size="small" :type="row.enabled ? 'danger' : ''" @click="toggle(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无用户数据" description="当前没有找到符合条件的用户。" />
      <div v-if="total > size" class="pager">
        <el-pagination layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="changePage" />
      </div>
    </section>

    <el-dialog v-model="detailVisible" title="用户详情" width="480px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="账号">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.nickname }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'
import EmptyState from '../components/EmptyState.vue'

const items = ref([]), total = ref(0), page = ref(1), size = 10, keyword = ref(''), detail = ref(null), detailVisible = ref(false)
async function load() {
  const { data } = await api.get('/auth/admin/users', { params: { keyword: keyword.value, page: page.value, size } })
  items.value = data.data.items || []; total.value = data.data.total || 0
}
async function toggle(row) {
  try {
    await ElMessageBox.confirm(`确定要${row.enabled ? '禁用' : '启用'}该用户吗？`, '确认操作', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await api.put(`/auth/admin/users/${row.id}/enabled`, null, { params: { enabled: !row.enabled } })
    ElMessage.success(row.enabled ? '已禁用' : '已启用')
    load()
  } catch { /* user cancelled */ }
}
function openDetail(row) { detail.value = row; detailVisible.value = true }
function changePage(v) { page.value = v; load() }
onMounted(load)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
