<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchCategories, createCategory, updateCategory, deleteCategory } from '@/api/product'

const categories = ref<any[]>([]); const dialogVisible = ref(false); const editing = ref<any>(null)
const form = ref({ name: '', parentId: null as number | null, sortOrder: 0 })

function resetForm() { form.value = { name: '', parentId: null, sortOrder: 0 }; editing.value = null }
function openAdd(parentId?: number) { resetForm(); if (parentId) form.value.parentId = parentId; dialogVisible.value = true }
function openEdit(c: any) { editing.value = c; form.value = { name: c.name, parentId: c.parentId, sortOrder: c.sortOrder ?? 0 }; dialogVisible.value = true }

async function load() {
  try { const res: any = await fetchCategories(); categories.value = res.data ?? res } catch { categories.value = [] }
}
async function handleSave() {
  try { if (editing.value) await updateCategory({ id: editing.value.id, ...form.value }); else await createCategory(form.value as any); ElMessage.success('已保存'); dialogVisible.value = false; load() } catch { /* handled */ }
}
async function handleDelete(id: number) {
  try { await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }); await deleteCategory(id); ElMessage.success('已删除'); load() } catch { /* cancelled */ }
}
onMounted(load)
</script>
<template>
  <div>
    <div class="tb-header"><h2>分类管理</h2><el-button type="primary" @click="openAdd()">新增分类</el-button></div>
    <el-table :data="categories" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" />
      <el-table-column label="层级" width="80"><template #default="{row}">{{row.parentId?'二级':'一级'}}</template></el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button text size="small" @click="openEdit(row)">编辑</el-button>
          <el-button text size="small" @click="openAdd(row.id)" v-if="!row.parentId">加子分类</el-button>
          <el-button text size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editing?'编辑分类':'新增分类'" width="400px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>
.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.tb-header h2 { font-size: 20px; font-weight: 700; }
</style>
