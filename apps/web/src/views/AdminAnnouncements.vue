<template>
  <AdminLayout>
    <AdminPageHeader title="公告管理" eyebrow="Announcement" subtitle="发布和管理商城公告信息。" />

    <section class="toolbar-panel">
      <el-form :inline="true" :model="form">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="公告标题" style="width:200px" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" placeholder="公告内容" style="width:300px" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="save">{{ form.id ? '保存修改' : '发布公告' }}</el-button>
          <el-button v-if="form.id" @click="form = { title: '', content: '' }">取消</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="发布时间" width="170" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link size="small" @click="form = { ...row }">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无公告数据" description="点击发布公告创建第一条公告。" />
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

const items = ref([]), form = ref({ title: '', content: '' })
async function load() { items.value = (await api.get('/announcements')).data.data || [] }
async function save() {
  if (!form.value.title) return ElMessage.warning('请输入公告标题')
  if (form.value.id) { await api.put('/admin/announcements', form.value) }
  else { await api.post('/admin/announcements', form.value) }
  ElMessage.success('已保存'); form.value = { title: '', content: '' }; load()
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete(`/admin/announcements/${id}`)
    ElMessage.success('公告已删除')
    load()
  } catch { /* user cancelled */ }
}
onMounted(load)
</script>
