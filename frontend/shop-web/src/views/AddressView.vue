<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress, type UserAddress } from '@/api/address'

const addresses = ref<UserAddress[]>([])
const dialogVisible = ref(false)
const editing = ref<UserAddress | null>(null)
const form = ref({ receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })

function resetForm() {
  form.value = { receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false }
  editing.value = null
}

function openAdd() { resetForm(); dialogVisible.value = true }
function openEdit(a: UserAddress) {
  editing.value = a
  form.value = { receiver: a.receiver, phone: a.phone, province: a.province, city: a.city, district: a.district, detail: a.detail, isDefault: a.isDefault }
  dialogVisible.value = true
}

async function handleSave() {
  const f = form.value
  if (!f.receiver || !f.phone || !f.province || !f.city || !f.district || !f.detail) {
    ElMessage.warning('请填写完整地址信息'); return
  }
  try {
    if (editing.value) {
      await updateAddress({ ...editing.value, ...f } as UserAddress)
    } else {
      await createAddress(f as any)
    }
    ElMessage.success('已保存')
    dialogVisible.value = false
    load()
  } catch { /* handled */ }
}

async function handleDelete(a: UserAddress) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    await deleteAddress(a.id)
    ElMessage.success('已删除')
    load()
  } catch { /* cancelled */ }
}

async function handleSetDefault(a: UserAddress) {
  await setDefaultAddress(a.id)
  ElMessage.success('已设为默认')
  load()
}

async function load() {
  try {
    const res: any = await fetchAddresses()
    addresses.value = res.data ?? []
  } catch { addresses.value = [] }
}

onMounted(load)
</script>

<template>
  <div class="page-container addr-page">
    <div class="page-intro">
      <div>
        <h2 class="page-title">收货地址</h2>
        <p>维护常用地址并设置默认地址，结算页会读取同一份地址数据。</p>
      </div>
      <el-button type="primary" @click="openAdd">新增地址</el-button>
    </div>
    <div class="page-metrics">
      <div class="metric-card"><span>地址数量</span><strong>{{ addresses.length }}</strong><small>当前用户地址</small></div>
      <div class="metric-card"><span>默认地址</span><strong>{{ addresses.some(a => a.isDefault) ? '已设' : '未设' }}</strong><small>结算优先使用</small></div>
      <div class="metric-card"><span>操作</span><strong>增删改</strong><small>完整管理能力</small></div>
    </div>
    <div v-for="a in addresses" :key="a.id" class="addr-card">
      <div class="addr-info">
        <div class="addr-receiver">{{ a.receiver }} <span class="addr-phone">{{ a.phone }}</span></div>
        <div class="addr-detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</div>
      </div>
      <div class="addr-actions">
        <span v-if="a.isDefault" class="addr-tag">默认</span>
        <el-button text size="small" @click="handleSetDefault(a)">设为默认</el-button>
        <el-button text size="small" @click="openEdit(a)">编辑</el-button>
        <el-button text size="small" type="danger" @click="handleDelete(a)">删除</el-button>
      </div>
    </div>
    <div v-if="!addresses.length" class="empty"><el-empty description="暂无收货地址" /></div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑地址' : '新增地址'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="收货人"><el-input v-model="form.receiver" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="form.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="区/县"><el-input v-model="form.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.detail" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="form.isDefault" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>
.addr-page { max-width: 700px; }
.addr-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.addr-header h2 { font-size: 22px; font-weight: 700; }
.addr-card { background: #fff; border-radius: 12px; padding: 16px 20px; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.addr-receiver { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.addr-phone { font-weight: 400; font-size: 13px; color: #888; }
.addr-detail { font-size: 13px; color: #666; }
.addr-actions { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }
.addr-tag { font-size: 11px; color: var(--color-primary); background: #fff0eb; padding: 1px 6px; border-radius: 3px; }
</style>
