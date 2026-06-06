<template>
  <section>
    <div class="page-head">
      <div>
        <h1>收货地址</h1>
        <p class="muted">管理你的收货地址信息。</p>
      </div>
    </div>

    <section class="page-card">
      <h2>{{ form.id ? '编辑地址' : '新增地址' }}</h2>
      <el-form :inline="true" :model="form" class="addr-form">
        <el-form-item label="收货人"><el-input v-model="form.receiverName" placeholder="请输入收货人" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="省份"><el-input v-model="form.province" placeholder="省份" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="form.city" placeholder="城市" /></el-form-item>
        <el-form-item label="区县"><el-input v-model="form.district" placeholder="区县" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.detailAddress" placeholder="街道、门牌号等" style="width:320px" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="form.isDefault" /></el-form-item>
        <el-form-item>
          <el-button type="danger" @click="save">{{ form.id ? '保存修改' : '新增地址' }}</el-button>
          <el-button v-if="form.id" @click="form = empty()">取消编辑</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="page-card">
      <el-table v-if="addresses.length" :data="addresses" class="addr-table">
        <el-table-column prop="receiverName" label="收货人" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="地址" min-width="220">
          <template #default="{ row }">{{ row.province }}{{ row.city }}{{ row.district }}{{ row.detailAddress }}</template>
        </el-table-column>
        <el-table-column label="默认" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="danger" size="small">默认</el-tag>
            <span v-else class="muted">否</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button link size="small" @click="edit(row)">编辑</el-button>
            <el-button link size="small" @click="setDefault(row.id)">设为默认</el-button>
            <el-button link type="danger" size="small" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无收货地址" description="新增地址后可在此管理和选择收货地址。" />
    </section>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'
import { useUserStore, useCartStore, useFavoriteStore } from '@/stores'
import EmptyState from '../components/EmptyState.vue'

const userStore = useUserStore(), addresses = ref<any[]>([])
const empty = () => ({ userId: userStore.userId, receiverName: '', phone: '', province: '', city: '', district: '', detailAddress: '', isDefault: false })
const form = ref<any>(empty())

async function load() { addresses.value = (await api.get('/addresses', { params: { userId: userStore.userId } })).data.data || [] }
async function save() {
  if (!form.value.receiverName || !form.value.phone) return ElMessage.warning('请填写收货人和手机号')
  if (form.value.id) { await api.put('/addresses', form.value); ElMessage.success('地址已更新') }
  else { await api.post('/addresses', form.value); ElMessage.success('地址已新增') }
  form.value = empty(); load()
}
function edit(row) { form.value = { ...row } }
async function setDefault(id) { await api.put(`/addresses/${id}/default`, null, { params: { userId: userStore.userId } }); ElMessage.success('已设为默认地址'); load() }
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete(`/addresses/${id}`)
    ElMessage.success('地址已删除')
    load()
  } catch { /* user cancelled */ }
}

onMounted(load)
</script>

<style scoped>
.page-head { margin-bottom: 20px; }
h1, p { margin: 0; }
h1 { font-size: 22px; }
p { color: var(--muted); margin-top: 4px; }
.page-card { margin-bottom: 16px; }
.page-card h2 { margin: 0 0 16px; font-size: 16px; font-weight: 650; }
.addr-form { display: flex; flex-wrap: wrap; gap: 8px; }
.addr-table :deep(th) { background: #f8fafc; }
</style>
