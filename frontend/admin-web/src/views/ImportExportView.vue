<template>
  <AdminLayout>
    <div class="page-wrap">
      <AdminPageHeader title="数据导入导出" description="批量导入商品数据，导出订单、用户与商品报表" />

      <section class="admin-card block">
        <h2>📥 商品导入</h2>
        <p class="muted">支持 CSV 格式批量导入商品数据，表头顺序需为：分类ID、名称、价格、库存、图片、详情、参数。</p>
        <el-upload
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          accept=".csv"
          :limit="1"
        >
          <el-icon :size="48"><UploadFilled /></el-icon>
          <div class="upload-text">将 CSV 文件拖到此处，或点击上传</div>
          <template #tip>
            <div class="upload-tip">仅支持 .csv 文件，单次最多 500 条</div>
          </template>
        </el-upload>
        <el-button
          type="danger"
          :loading="importing"
          :disabled="!selectedFile"
          style="margin-top:14px"
          @click="doImport"
        >
          开始导入
        </el-button>
        <el-button style="margin-top:14px" @click="downloadTemplate">下载模板</el-button>
      </section>

      <section class="admin-card block">
        <h2>📤 数据导出</h2>
        <div class="export-grid">
          <article class="export-card" v-for="item in exportTypes" :key="item.key">
            <div>
              <strong>{{ item.label }}</strong>
              <p class="muted">{{ item.desc }}</p>
            </div>
            <el-button type="danger" :loading="exporting === item.key" @click="doExport(item.key)">
              导出 Excel
            </el-button>
          </article>
        </div>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { api } from '@/api'
import AdminLayout from '@/layouts/AdminLayout.vue'
import AdminPageHeader from '@/components/AdminPageHeader.vue'

const selectedFile = ref<File | null>(null)
const importing = ref(false)
const exporting = ref<string | null>(null)

const exportTypes = [
  { key: 'orders', label: '订单报表', desc: '导出所有订单数据，含金额、支付和状态' },
  { key: 'users', label: '用户列表', desc: '导出注册用户基础信息与账号状态' },
  { key: 'products', label: '商品清单', desc: '导出商品资料、库存和销量' },
]

const exportRouteMap: Record<string, string> = {
  orders: '/admin/orders/export',
  users: '/admin/users/export',
  products: '/products/admin/export',
}

function handleFileChange(file: any) {
  selectedFile.value = file.raw || null
}

async function doImport() {
  if (!selectedFile.value) return
  importing.value = true
  try {
    const fd = new FormData()
    fd.append('file', selectedFile.value)
    const res = await api.post('/products/admin/import', fd)
    ElMessage.success(`成功导入 ${res.data.data?.count || 0} 条商品数据`)
    selectedFile.value = null
  } catch {
    ElMessage.error('导入失败，请检查 CSV 格式')
  } finally {
    importing.value = false
  }
}

function downloadTemplate() {
  const csv = 'categoryId,name,price,stock,imageUrl,detailHtml,paramsText\n1,示例商品,99.00,100,/uploads/demo.png,商品详情,商品参数\n'
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = '商品导入模板.csv'; a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('模板已下载')
}

async function doExport(key: string) {
  exporting.value = key
  try {
    const route = exportRouteMap[key]
    if (!route) throw new Error(`unknown export key: ${key}`)
    const res = await api.get(route, { responseType: 'blob' })
    const url = URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url; a.download = `${key}_${new Date().toISOString().slice(0, 10)}.xlsx`; a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出完成')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = null
  }
}
</script>

<style scoped>
.block { margin-bottom: 20px; }
.block h2 { margin: 0 0 12px; font-size: 17px; font-weight: 700; }

.upload-text { margin-top: 8px; font-size: 14px; color: var(--muted); }
.upload-tip { font-size: 12px; color: var(--muted-light); margin-top: 4px; }

.export-grid { display: grid; gap: 12px; }
.export-card {
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  padding: 16px 18px;
  background: var(--panel-alt);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}
.export-card strong { font-size: 15px; }
.export-card p { margin: 4px 0 0; font-size: 13px; }

@media (max-width: 768px) {
  .export-card { flex-direction: column; align-items: flex-start; }
}
</style>
