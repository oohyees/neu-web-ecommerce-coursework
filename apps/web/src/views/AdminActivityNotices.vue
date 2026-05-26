<template>
  <AdminLayout>
    <AdminPageHeader title="活动通知" eyebrow="Activity Notice" subtitle="发布和管理商城活动通知。" />

    <section class="toolbar-panel">
      <el-input v-model="keyword" placeholder="搜索标题" style="width:220px" clearable @keyup.enter="load" />
      <el-button type="danger" @click="load">搜索</el-button>
    </section>

    <section class="toolbar-panel">
      <el-form :inline="true" :model="form">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="通知标题" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" placeholder="通知内容" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
        <el-form-item>
          <el-button type="danger" @click="save">{{ form.id ? '保存修改' : '新增' }}</el-button>
          <el-button v-if="form.id" @click="form = { title: '', content: '', enabled: true }">取消</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link size="small" @click="form = { ...row }">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无活动通知" description="点击新增按钮发布第一条活动通知。" />
    </section>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'
import EmptyState from '../components/EmptyState.vue'

const items = ref([]), keyword = ref(''), form = ref({ title: '', content: '', enabled: true })
async function load() { items.value = (await api.get('/admin/activity-notices', { params: { keyword: keyword.value } })).data.data || [] }
async function save() {
  if (!form.value.title) return ElMessage.warning('请输入通知标题')
  if (form.value.id) { await api.put('/admin/activity-notices', form.value) }
  else { await api.post('/admin/activity-notices', form.value) }
  ElMessage.success('已保存'); form.value = { title: '', content: '', enabled: true }; load()
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该活动通知吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete(`/admin/activity-notices/${id}`)
    ElMessage.success('已删除'); load()
  } catch { /* user cancelled */ }
}
onMounted(load)
</script>
