<template>
  <AdminLayout>
    <AdminPageHeader title="商品管理" eyebrow="Product" subtitle="维护商品资料、图片、库存、上下架和批量导入导出。">
      <el-upload :show-file-list="false" :http-request="importProducts"><el-button>导入商品</el-button></el-upload>
      <el-button @click="exportProducts">导出商品</el-button>
      <el-button type="danger" @click="openCreate">新增商品</el-button>
    </AdminPageHeader>

    <section class="toolbar-panel">
      <el-input v-model="keyword" placeholder="搜索商品名称" style="width:240px" clearable @keyup.enter="load" />
      <el-select v-model="categoryFilter" clearable placeholder="分类筛选" style="width:160px" @change="load">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="danger" @click="load">搜索</el-button>
    </section>

    <section class="admin-card">
      <el-table v-if="filteredProducts.length" :data="paginated" class="product-table">
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="goods-cell">
              <img :src="row.imageUrl" @error="imgFallback" />
              <div>
                <strong>{{ row.name }}</strong>
                <span class="muted">ID {{ row.id }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110">
          <template #default="{ row }"><span class="price">&yen;{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column prop="sales" label="销量" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isOnSale ? 'success' : 'info'" size="small">{{ row.isOnSale ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="danger" size="small" @click="edit(row)">编辑</el-button>
              <el-button link size="small" @click="toggleSale(row)">{{ row.isOnSale ? '下架' : '上架' }}</el-button>
              <el-button link type="danger" size="small" @click="remove(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无商品数据" description="请先新增商品或调整筛选条件。" />
      <div v-if="filteredProducts.length > size" class="pager">
        <el-pagination layout="prev, pager, next, total" :total="filteredProducts.length" :page-size="size" :current-page="page" @current-change="changePage" />
      </div>
    </section>

    <el-drawer v-model="drawerVisible" :title="form.id ? '编辑商品' : '新增商品'" size="560px">
      <el-form :model="form" label-width="82px">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="销量"><el-input-number v-model="form.sales" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="上架"><el-switch v-model="form.isOnSale" /></el-form-item>
        <el-form-item label="参数"><el-input v-model="form.paramsText" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="详情"><el-input v-model="form.detailHtml" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="图片">
          <el-upload :show-file-list="false" :http-request="uploadImage"><el-button>上传图片</el-button></el-upload>
          <img v-if="form.imageUrl" :src="form.imageUrl" class="preview" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="danger" @click="save">保存商品</el-button>
      </template>
    </el-drawer>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'
import EmptyState from '../components/EmptyState.vue'

const products = ref([])
const page = ref(1)
const size = 10
const categories = ref([])
const keyword = ref('')
const categoryFilter = ref(null)
const drawerVisible = ref(false)
const emptyForm = () => ({ categoryId: categories.value[0]?.id || null, name: '', price: 0, stock: 0, sales: 0, isOnSale: true, imageUrl: '', detailHtml: '', paramsText: '' })
const form = ref(emptyForm())
const filteredProducts = computed(() => products.value.filter(p => (!keyword.value || p.name?.includes(keyword.value)) && (!categoryFilter.value || p.categoryId === categoryFilter.value)))
const paginated = computed(() => {
  const start = (page.value - 1) * size
  return filteredProducts.value.slice(start, start + size)
})

async function load() {
  const result = (await api.get('/products/admin/all', { params: { page: 1, size: 999 } })).data.data
  products.value = result.items || []
  page.value = 1
  categories.value = (await api.get('/categories')).data.data || []
  if (!form.value.categoryId && categories.value.length) form.value.categoryId = categories.value[0].id
}

function openCreate() { form.value = emptyForm(); drawerVisible.value = true }
async function save() {
  if (!form.value.name) return ElMessage.warning('请输入商品名称')
  if (form.value.id) {
    await api.put('/products/admin', form.value)
    ElMessage.success('商品已更新')
  } else {
    await api.post('/products/admin', form.value)
    ElMessage.success('商品已新增')
  }
  drawerVisible.value = false; load()
}
function edit(row) { form.value = { ...row }; drawerVisible.value = true }

async function remove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？此操作不可恢复。', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete(`/products/admin/${id}`)
    ElMessage.success('商品已删除')
    load()
  } catch { /* user cancelled */ }
}

async function toggleSale(row) {
  await api.put('/products/admin', { ...row, isOnSale: !row.isOnSale })
  ElMessage.success(row.isOnSale ? '已下架' : '已上架')
  load()
}

function changePage(v) { page.value = v }
async function uploadImage({ file }) { const fd = new FormData(); fd.append('file', file); form.value.imageUrl = (await api.post('/files/upload', fd)).data.data }
async function importProducts({ file }) { const fd = new FormData(); fd.append('file', file); await api.post('/products/admin/import', fd); ElMessage.success('导入完成'); load() }
async function exportProducts() { const response = await api.get('/products/admin/export', { responseType: 'blob' }); const url = URL.createObjectURL(response.data); const a = document.createElement('a'); a.href = url; a.download = 'products.xlsx'; a.click(); URL.revokeObjectURL(url) }
function imgFallback(e) {
  e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 56 56"><rect fill="%23f3f4f6" width="56" height="56"/><text x="28" y="28" text-anchor="middle" dy=".35em" fill="%239ca3af" font-size="8">无图</text></svg>'
}

onMounted(load)
</script>

<style scoped>
.product-table :deep(th) { background: #f8fafc; }
.goods-cell { display: flex; gap: 12px; align-items: center; }
.goods-cell img { width: 56px; height: 56px; object-fit: contain; border: 1px solid #f1f5f9; border-radius: 6px; }
.goods-cell div { display: grid; gap: 5px; }
.goods-cell strong { font-size: 14px; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
.preview { display: block; width: 120px; height: 90px; margin-top: 10px; object-fit: contain; border: 1px solid var(--line); border-radius: 6px; }
</style>
