<template>
  <AdminLayout>
    <AdminPageHeader title="分类管理" eyebrow="Category" subtitle="维护商品分类、排序和层级关系。" />

    <section class="toolbar-panel">
      <el-form :inline="true" :model="form">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="form.parentId" placeholder="无（顶级分类）" clearable style="width:160px">
            <el-option v-for="c in parentCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="save">{{ form.id ? '保存修改' : '新增分类' }}</el-button>
          <el-button v-if="form.id" @click="resetForm">取消编辑</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="父分类" width="150">
          <template #default="{ row }">
            <span class="muted">{{ row.parentId ? (items.find(c => c.id === row.parentId)?.name || '-') : '顶级分类' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link size="small" @click="form = { ...row }">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无分类数据" description="请先新增商品分类。" />
    </section>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'
import EmptyState from '../components/EmptyState.vue'

const items = ref([])
const form = ref({ name: '', sortOrder: 0, parentId: null })
const parentCategories = computed(() => items.value.filter(c => !c.parentId))

async function load() { items.value = (await api.get('/categories')).data.data || [] }
function resetForm() { form.value = { name: '', sortOrder: 0, parentId: null } }
async function save() {
  if (!form.value.name) return ElMessage.warning('请输入分类名称')
  if (form.value.id) {
    await api.put('/admin/categories', form.value)
    ElMessage.success('分类已更新')
  } else {
    await api.post('/admin/categories', form.value)
    ElMessage.success('分类已新增')
  }
  resetForm(); load()
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete(`/admin/categories/${id}`)
    ElMessage.success('分类已删除')
    load()
  } catch { /* user cancelled */ }
}
onMounted(load)
</script>
