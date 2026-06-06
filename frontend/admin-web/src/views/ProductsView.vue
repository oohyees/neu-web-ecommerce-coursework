<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAdminProducts, createProduct, updateProduct, deleteProduct, forceDeleteProduct, exportProducts, importProducts, fetchCategories } from '@/api/product'

const products = ref<any[]>([])
const categories = ref<any[]>([])
const total = ref(0); const page = ref(1); const keyword = ref('')
const dialogVisible = ref(false); const editing = ref<any>(null)
const form = ref<any>({}); const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const [pRes, cRes] = await Promise.all([fetchAdminProducts({ page: page.value, size: 10, keyword: keyword.value || undefined }), fetchCategories()])
    products.value = (pRes as any).data?.items ?? []; total.value = (pRes as any).data?.total ?? 0; categories.value = cRes as any
  } catch { products.value = [] }
  finally { loading.value = false }
}
function resetForm() { form.value = { name:'', categoryId:null, price:0, stock:0, imageUrl:'', detailHtml:'', paramsText:'', isOnSale:true }; editing.value = null }
function openAdd() { resetForm(); dialogVisible.value = true }
function openEdit(p: any) { editing.value = p; form.value = { ...p }; dialogVisible.value = true }
async function handleSave() {
  try { if (editing.value) await updateProduct(form.value); else await createProduct(form.value); ElMessage.success('已保存'); dialogVisible.value = false; load() } catch { /* handled */ }
}
async function handleDelete(p: any) { try { await ElMessageBox.confirm('确定删除？','提示',{type:'warning'}); await deleteProduct(p.id); ElMessage.success('已下架'); load() } catch { /* cancelled */ } }
async function handleForceDelete(p: any) { try { await ElMessageBox.confirm('物理删除不可恢复！','警告',{type:'error'}); await forceDeleteProduct(p.id); ElMessage.success('已删除'); load() } catch { /* cancelled */ } }
async function handleImport(e: Event) { const f = (e.target as HTMLInputElement).files?.[0]; if (f) { try { await importProducts(f); ElMessage.success('导入成功'); load() } catch { /* handled */ } } }
onMounted(load)
</script>
<template>
  <div>
    <div class="tb-header">
      <h2>商品管理</h2>
      <div class="tb-actions">
        <el-input v-model="keyword" placeholder="搜索商品" size="default" style="width:200px" clearable @change="load" />
        <el-button type="primary" @click="openAdd">新增</el-button>
        <el-button @click="exportProducts()">导出Excel</el-button>
        <label class="import-btn"><input type="file" accept=".csv" hidden @change="handleImport" />导入CSV</label>
      </div>
    </div>
    <el-table :data="products" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="90" />
      <el-table-column prop="stock" label="库存" width="70" />
      <el-table-column prop="sales" label="销量" width="70" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.isOnSale?'success':'danger'" size="small">{{row.isOnSale?'上架':'下架'}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button text size="small" @click="openEdit(row)">编辑</el-button>
          <el-button text size="small" type="danger" @click="handleDelete(row)">下架</el-button>
          <el-button text size="small" type="danger" @click="handleForceDelete(row)">物理删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" style="margin-top:16px;justify-content:flex-end" @change="load" />
    <el-dialog v-model="dialogVisible" :title="editing?'编辑商品':'新增商品'" width="600px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.categoryId"><el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :step="0.01" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-form-item label="详情"><el-input v-model="form.detailHtml" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="参数"><el-input v-model="form.paramsText" /></el-form-item>
        <el-form-item label="上架"><el-switch v-model="form.isOnSale" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>
.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.tb-header h2 { font-size: 20px; font-weight: 700; }
.tb-actions { display: flex; gap: 8px; align-items: center; }
.import-btn { display: inline-flex; align-items: center; padding: 8px 15px; border: 1px solid #dcdfe6; border-radius: 8px; font-size: 14px; cursor: pointer; color: #606266; }
.import-btn:hover { color: var(--color-primary); border-color: var(--color-primary); }
</style>
