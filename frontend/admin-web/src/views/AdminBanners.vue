<template>
  <AdminLayout>
    <AdminPageHeader title="轮播管理" eyebrow="Banner" subtitle="管理首页轮播图、标题、链接和排序。" />

    <section class="toolbar-panel">
      <el-input v-model="keyword" placeholder="搜索标题" style="width:220px" clearable @keyup.enter="load" />
      <el-button type="danger" @click="load">搜索</el-button>
    </section>

    <el-alert v-if="form.id" title="正在编辑轮播图" type="warning" :closable="false" class="editing" />

    <section class="toolbar-panel">
      <el-form :inline="true" :model="form">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="轮播标题" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="form.linkUrl" placeholder="/products" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="图片">
          <el-upload :show-file-list="false" :http-request="uploadImage"><el-button>上传图片</el-button></el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="save">{{ form.id ? '保存修改' : '新增' }}</el-button>
          <el-button v-if="form.id" @click="cancelEdit">取消编辑</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="imageUrl" label="图片地址" min-width="260" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link size="small" @click="edit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无轮播图" description="点击新增按钮添加首页轮播图。" />
    </section>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'
import EmptyState from '../components/EmptyState.vue'

const items = ref<any[]>([]), form = ref<any>({ title: '', imageUrl: '', linkUrl: '/products', sortOrder: 0 }), keyword = ref('')
async function load() { items.value = (await api.get('/home/banners', { params: { keyword: keyword.value } })).data.data || [] }
async function save() {
  if (!form.value.title) return ElMessage.warning('请输入轮播标题')
  if (form.value.id) { await api.put('/home/banners', form.value) }
  else { await api.post('/home/banners', form.value) }
  ElMessage.success('已保存'); cancelEdit(); load()
}
async function uploadImage({ file }) { const fd = new FormData(); fd.append('file', file); form.value.imageUrl = (await api.post('/files/upload', fd)).data.data }
function edit(row) { form.value = { ...row } }
function cancelEdit() { form.value = { title: '', imageUrl: '', linkUrl: '/products', sortOrder: 0 } }
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该轮播图吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete(`/home/banners/${id}`)
    ElMessage.success('已删除'); load()
  } catch { /* user cancelled */ }
}
onMounted(load)
</script>

<style scoped>
.editing { margin-bottom: 12px; }
</style>
