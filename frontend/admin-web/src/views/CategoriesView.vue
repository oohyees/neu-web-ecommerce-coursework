<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
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
const rootCount = computed(() => categories.value.filter((c) => !c.parentId).length)
const childCount = computed(() => categories.value.filter((c) => c.parentId).length)
onMounted(load)
</script>
<template>
  <div class="admin-page categories-page">
    <div class="tb-header">
      <div>
        <h2>分类管理</h2>
        <p class="page-subtitle">维护一级/二级分类和排序，首页分类入口与商品筛选都依赖这里。</p>
      </div>
      <div class="tb-actions">
        <el-button type="primary" :icon="Plus" @click="openAdd()">新增分类</el-button>
      </div>
    </div>
    <div class="admin-summary">
      <div class="summary-card"><div class="summary-card__label">全部分类</div><div class="summary-card__value">{{ categories.length }}</div><div class="summary-card__hint">当前可配置类目</div></div>
      <div class="summary-card"><div class="summary-card__label">一级分类</div><div class="summary-card__value">{{ rootCount }}</div><div class="summary-card__hint">首页主入口</div></div>
      <div class="summary-card"><div class="summary-card__label">二级分类</div><div class="summary-card__value">{{ childCount }}</div><div class="summary-card__hint">细分筛选入口</div></div>
      <div class="summary-card"><div class="summary-card__label">排序字段</div><div class="summary-card__value">sort</div><div class="summary-card__hint">低值优先展示</div></div>
    </div>
    <div class="table-panel">
      <el-table :data="categories" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="分类名称" min-width="180">
          <template #default="{row}">
            <strong>{{ row.name }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="层级" width="120"><template #default="{row}"><span :class="['status-pill', row.parentId ? 'status-pill--info' : 'status-pill--success']">{{row.parentId?'二级':'一级'}}</span></template></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{row}">
            <div class="action-stack">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" @click="openAdd(row.id)" v-if="!row.parentId">加子分类</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
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
.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }
</style>
