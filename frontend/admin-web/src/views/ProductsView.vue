<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus, Search, Upload } from '@element-plus/icons-vue'
import { fetchAdminProducts, createProduct, updateProduct, deleteProduct, forceDeleteProduct, exportProducts, importProducts, fetchCategories } from '@/api/product'

const products = ref<any[]>([])
const categories = ref<any[]>([])
const total = ref(0); const page = ref(1); const keyword = ref('')
const categoryId = ref<number | undefined>()
const saleStatus = ref<string>('')
const dialogVisible = ref(false); const editing = ref<any>(null)
const form = ref<any>({}); const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: 10, keyword: keyword.value || undefined }
    if (categoryId.value) params.categoryId = categoryId.value
    if (saleStatus.value) params.isOnSale = saleStatus.value === 'on'
    const [pRes, cRes] = await Promise.all([fetchAdminProducts(params), fetchCategories()])
    products.value = (pRes as any).data?.items ?? []; total.value = (pRes as any).data?.total ?? 0; categories.value = cRes as any
  } catch { products.value = [] }
  finally { loading.value = false }
}
function imageUrl(p: any) {
  const url = p.imageUrl || p.mainImage || p.image || ''
  if (!url) return '/catalog/placeholder.svg'
  if (/^https?:\/\//i.test(url)) return url
  return url.startsWith('/') ? url : `/${url}`
}
function categoryName(id: number) {
  return categories.value.find((c: any) => c.id === id)?.name || '未分类'
}
function productParam(product: any, name: string) {
  if (!product.paramsText) return ''
  const item = String(product.paramsText).split(';').find((part) => part.trim().startsWith(`${name}:`))
  return item?.split(':').slice(1).join(':').trim() || ''
}
function applySearch() { page.value = 1; load() }
function resetFilters() { keyword.value = ''; categoryId.value = undefined; saleStatus.value = ''; page.value = 1; load() }
function resetForm() { form.value = { name:'', subtitle:'', categoryId:null, price:0, originalPrice:null, stock:0, imageUrl:'', detailHtml:'', paramsText:'', isOnSale:true }; editing.value = null }
function openAdd() { resetForm(); dialogVisible.value = true }
function openEdit(p: any) { editing.value = p; form.value = { ...p }; dialogVisible.value = true }
async function handleSave() {
  try { if (editing.value) await updateProduct(form.value); else await createProduct(form.value); ElMessage.success('已保存'); dialogVisible.value = false; load() } catch { /* handled */ }
}
async function handleDelete(p: any) { try { await ElMessageBox.confirm('确定删除？','提示',{type:'warning'}); await deleteProduct(p.id); ElMessage.success('已下架'); load() } catch { /* cancelled */ } }
async function handleForceDelete(p: any) { try { await ElMessageBox.confirm('物理删除不可恢复！','警告',{type:'error'}); await forceDeleteProduct(p.id); ElMessage.success('已删除'); load() } catch { /* cancelled */ } }
async function handleImport(e: Event) { const f = (e.target as HTMLInputElement).files?.[0]; if (f) { try { await importProducts(f); ElMessage.success('导入成功'); load() } catch { /* handled */ } } }
const onSaleCount = computed(() => products.value.filter((p) => p.isOnSale).length)
const offSaleCount = computed(() => products.value.filter((p) => !p.isOnSale).length)
const lowStockCount = computed(() => products.value.filter((p) => Number(p.stock || 0) > 0 && Number(p.stock || 0) <= 5).length)
const outStockCount = computed(() => products.value.filter((p) => Number(p.stock || 0) <= 0).length)
onMounted(load)
</script>
<template>
  <div class="admin-page products-page">
    <div class="tb-header">
      <div>
        <h2>商品管理</h2>
        <p class="page-subtitle">维护商品、库存、上下架与导入导出，课堂演示时优先展示图片和库存状态。</p>
      </div>
      <div class="tb-actions">
        <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索商品" size="default" style="width:220px" clearable @keyup.enter="applySearch" @clear="applySearch" />
        <el-select v-model="categoryId" placeholder="分类" size="default" style="width:150px" clearable @change="applySearch">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="saleStatus" placeholder="状态" size="default" style="width:120px" clearable @change="applySearch">
          <el-option label="上架" value="on" />
          <el-option label="下架" value="off" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增</el-button>
        <el-button :icon="Download" @click="exportProducts()">导出Excel</el-button>
        <label class="import-btn"><el-icon><Upload /></el-icon><input type="file" accept=".csv" hidden @change="handleImport" />导入CSV</label>
      </div>
    </div>

    <div class="admin-summary">
      <div class="summary-card"><div class="summary-card__label">当前页上架</div><div class="summary-card__value">{{ onSaleCount }}</div><div class="summary-card__hint">可直接进入销售链路</div></div>
      <div class="summary-card"><div class="summary-card__label">当前页下架</div><div class="summary-card__value">{{ offSaleCount }}</div><div class="summary-card__hint">需检查库存或图片</div></div>
      <div class="summary-card"><div class="summary-card__label">低库存</div><div class="summary-card__value">{{ lowStockCount }}</div><div class="summary-card__hint">库存 1-5 件</div></div>
      <div class="summary-card"><div class="summary-card__label">缺货</div><div class="summary-card__value">{{ outStockCount }}</div><div class="summary-card__hint">课堂演示避免选择</div></div>
    </div>

    <div class="table-panel">
      <el-table :data="products" stripe v-loading="loading">
        <el-table-column label="商品" min-width="300">
          <template #default="{row}">
            <div class="product-cell">
              <img class="product-thumb" :src="imageUrl(row)" :alt="row.name" />
              <div class="product-meta">
                <strong>{{ row.name }}</strong>
                <span>ID {{ row.id }} · {{ categoryName(row.categoryId) }}</span>
                <small>{{ row.brand || productParam(row, '品牌') || productParam(row, '分类') || 'DummyJSON' }} · {{ productParam(row, 'SKU') || row.sku || '无 SKU' }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120"><template #default="{row}"><span class="money">¥{{ Number(row.price || 0).toFixed(2) }}</span></template></el-table-column>
        <el-table-column label="评分/折扣" width="130">
          <template #default="{row}">
            <div class="compact-cell">
              <span>★ {{ row.rating || productParam(row, '评分') || '-' }}</span>
              <small>{{ row.discountPercentage || productParam(row, '折扣') || '无折扣' }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column prop="sales" label="销量" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{row}">
            <span :class="['status-pill', row.isOnSale ? 'status-pill--success' : 'status-pill--danger']">{{row.isOnSale?'上架':'下架'}}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{row}">
            <div class="action-stack">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" :type="row.isOnSale ? 'warning' : 'success'" @click="handleDelete(row)">{{ row.isOnSale ? '下架' : '恢复' }}</el-button>
              <el-button size="small" type="danger" @click="handleForceDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-panel__footer">
        <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @change="load" />
      </div>
    </div>
    <el-dialog v-model="dialogVisible" :title="editing?'编辑商品':'新增商品'" width="600px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.categoryId"><el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :step="0.01" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :step="0.01" /></el-form-item>
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
.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }
.product-cell { display: flex; align-items: center; gap: 12px; min-width: 0; }
.product-thumb { width: 56px; height: 56px; border-radius: 8px; object-fit: cover; border: 1px solid #edf0f5; background: #f8fafc; }
.product-meta { display: grid; gap: 5px; min-width: 0; }
.product-meta strong { color: #1f2937; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-meta span { color: #8a94a6; font-size: 12px; }
.product-meta small { color: #f97316; font-size: 12px; font-weight: 700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.compact-cell { display: grid; gap: 3px; color: #f59e0b; font-size: 13px; font-weight: 800; }
.compact-cell small { color: #ef4444; font-size: 12px; font-weight: 700; }
</style>
