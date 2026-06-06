<template>
  <AdminLayout>
    <AdminPageHeader title="管理员账号管理" eyebrow="Admin Accounts" subtitle="管理后台管理员账号，分配 SUPER_ADMIN 或 ADMIN 角色。">
      <el-button type="danger" @click="openCreate">新增管理员</el-button>
    </AdminPageHeader>

    <section class="toolbar-panel">
      <el-input v-model="keyword" placeholder="搜索用户名/昵称" style="width:240px" clearable @keyup.enter="load" />
      <el-button type="danger" @click="load">搜索</el-button>
    </section>

    <section class="admin-card">
      <el-table v-if="items.length" :data="items">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">
            <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : 'warning'" size="small">
              {{ row.role === 'SUPER_ADMIN' ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link size="small" @click="edit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(row.id)" :disabled="row.username === 'admin'">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else title="暂无管理员数据" description="点击新增按钮添加管理员账号。" />
    </section>

    <el-dialog v-model="visible" :title="form.id ? '编辑管理员' : '新增管理员'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" v-if="!form.id">
          <el-input v-model="form.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="登录密码" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="邮箱地址" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="手机号码" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="普通管理员 (ADMIN)" value="ADMIN" />
            <el-option label="超级管理员 (SUPER_ADMIN)" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
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
const emptyForm = () => ({ username: '', password: '', nickname: '', email: '', phone: '', role: 'ADMIN' })
const form = ref<any>(emptyForm())

async function load() { items.value = (await api.get('/auth/admin/admins', { params: { keyword: keyword.value } })).data.data || [] }
function openCreate() { form.value = emptyForm(); visible.value = true }
function edit(row) { form.value = { ...row, username: row.username }; visible.value = true }
async function save() {
  if (!form.value.id && (!form.value.username || !form.value.password)) return ElMessage.warning('请填写用户名和密码')
  if (form.value.id) {
    await api.put('/auth/admin/admins', form.value)
    ElMessage.success('管理员已更新')
  } else {
    await api.post('/auth/admin/admins', form.value)
    ElMessage.success('管理员已新增')
  }
  visible.value = false; load()
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该管理员账号吗？', '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await api.delete(`/auth/admin/admins/${id}`)
    ElMessage.success('已删除'); load()
  } catch { /* user cancelled */ }
}
onMounted(load)
</script>
