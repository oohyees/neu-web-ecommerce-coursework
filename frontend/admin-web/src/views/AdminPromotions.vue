<template>
  <AdminLayout>
    <AdminPageHeader title="促销管理" eyebrow="Promotion" subtitle="管理秒杀、直降等促销活动，设置商品、价格、时间和库存。">
      <el-button type="danger" @click="openCreate">新增促销</el-button>
    </AdminPageHeader>

    <section class="toolbar-panel">
      <el-input v-model="keyword" placeholder="搜索促销标题" style="width:240px" clearable @keyup.enter="load" />
      <el-button type="danger" @click="load">搜索</el-button>
    </section>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.promotionType === 'FLASH_SALE' ? 'danger' : 'warning'" size="small">
              {{ row.promotionType === 'FLASH_SALE' ? '秒杀' : row.promotionType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="促销价" width="110">
          <template #default="{ row }"><span class="price">&yen;{{ row.promotionPrice }}</span></template>
        </el-table-column>
        <el-table-column prop="promotionStock" label="库存" width="80" />
        <el-table-column prop="startAt" label="开始时间" width="170" />
        <el-table-column prop="endAt" label="结束时间" width="170" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link size="small" @click="edit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无促销活动" description="点击新增按钮创建促销活动。" />
    </section>

    <el-dialog v-model="visible" :title="form.id ? '编辑促销' : '新增促销'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="促销标题"><el-input v-model="form.title" placeholder="如：618大促" /></el-form-item>
        <el-form-item label="商品ID"><el-input-number v-model="form.productId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="促销类型">
          <el-select v-model="form.promotionType" style="width:100%">
            <el-option label="秒杀" value="FLASH_SALE" />
            <el-option label="直降" value="PROMOTION" />
            <el-option label="限时优惠" value="LIMITED_OFFER" />
          </el-select>
        </el-form-item>
        <el-form-item label="促销价格"><el-input-number v-model="form.promotionPrice" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="促销库存"><el-input-number v-model="form.promotionStock" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startAt" type="datetime" placeholder="选择开始时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endAt" type="datetime" placeholder="选择结束时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="danger" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'
import EmptyState from '../components/EmptyState.vue'

const items = ref<any[]>([]), keyword = ref(''), visible = ref(false)
const emptyForm = () => ({ title: '', productId: null, promotionType: 'FLASH_SALE', promotionPrice: 0, promotionStock: 100, startAt: '', endAt: '', enabled: true })
const form = ref(emptyForm())

async function load() { items.value = (await api.get('/marketing/admin/promotions', { params: { keyword: keyword.value } })).data.data || [] }
function openCreate() { form.value = emptyForm(); visible.value = true }
function edit(row) { form.value = { ...row }; visible.value = true }
async function save() {
  if (!form.value.title || !form.value.productId) return ElMessage.warning('请填写标题和商品ID')
  if (form.value.id) {
    await api.put('/marketing/admin/promotions', form.value)
    ElMessage.success('促销已更新')
  } else {
    await api.post('/marketing/admin/promotions', form.value)
    ElMessage.success('促销已新增')
  }
  visible.value = false; load()
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该促销吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete(`/marketing/admin/promotions/${id}`)
    ElMessage.success('已删除'); load()
  } catch { /* user cancelled */ }
}
onMounted(load)
</script>
