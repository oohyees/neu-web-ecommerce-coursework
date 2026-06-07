<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'
import { fetchAdmins, createAdmin, updateAdmin, deleteAdmin } from '@/api/permissionManage'

const adminStore = useAdminStore()
const admins = ref<any[]>([])
const dialogVisible = ref(false); const editing = ref<any>(null)
const form = ref({ username:'', password:'', nickname:'', email:'', phone:'', role:'ADMIN' })

function resetForm() { form.value = { username:'', password:'', nickname:'', email:'', phone:'', role:'ADMIN' }; editing.value = null }
function openAdd() { resetForm(); dialogVisible.value = true }
function openEdit(a: any) { editing.value = a; form.value = { username:a.username, password:'', nickname:a.nickname||'', email:a.email||'', phone:a.phone||'', role:a.role }; dialogVisible.value = true }

async function load() { try { const res: any = await fetchAdmins(); admins.value = res.data ?? [] } catch { admins.value = [] } }
async function handleSave() {
  try {
    if (editing.value) await updateAdmin({ id: editing.value.id, ...form.value, password: form.value.password || undefined } as any)
    else await createAdmin(form.value)
    ElMessage.success('已保存'); dialogVisible.value = false; load()
  } catch { /* handled */ }
}
async function handleDelete(id: number) { try { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deleteAdmin(id); ElMessage.success('已删除'); load() } catch { /* cancelled */ } }
const superCount = computed(() => admins.value.filter((a) => a.role === 'SUPER_ADMIN').length)
const adminCount = computed(() => admins.value.filter((a) => a.role !== 'SUPER_ADMIN').length)
onMounted(load)
</script>
<template>
  <div class="admin-page permission-page">
    <div class="tb-header">
      <div><h2>权限管理</h2><p class="page-subtitle">管理后台账号和角色，配合菜单权限与接口 403 作为权限验收证据。</p></div>
      <div class="tb-actions"><el-button v-if="adminStore.role==='SUPER_ADMIN'" type="primary" :icon="Plus" @click="openAdd">新增管理员</el-button></div>
    </div>
    <div class="admin-summary">
      <div class="summary-card"><div class="summary-card__label">管理员总数</div><div class="summary-card__value">{{ admins.length }}</div><div class="summary-card__hint">后台账号规模</div></div>
      <div class="summary-card"><div class="summary-card__label">超级管理员</div><div class="summary-card__value">{{ superCount }}</div><div class="summary-card__hint">最高权限</div></div>
      <div class="summary-card"><div class="summary-card__label">普通管理员</div><div class="summary-card__value">{{ adminCount }}</div><div class="summary-card__hint">运营权限</div></div>
      <div class="summary-card"><div class="summary-card__label">当前角色</div><div class="summary-card__value">{{ adminStore.role || 'ADMIN' }}</div><div class="summary-card__hint">决定菜单可见性</div></div>
    </div>
    <div class="table-panel">
    <el-table :data="admins" stripe>
      <el-table-column label="管理员" min-width="220"><template #default="{row}"><div class="admin-cell"><div class="avatar">{{ String(row.nickname || row.username || 'A').slice(0,1).toUpperCase() }}</div><div><strong>{{ row.nickname || row.username }}</strong><span>@{{ row.username }}</span></div></div></template></el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机" width="140" />
      <el-table-column prop="role" label="角色" width="150"><template #default="{row}"><span :class="['status-pill', row.role==='SUPER_ADMIN'?'status-pill--danger':'status-pill--info']">{{row.role}}</span></template></el-table-column>
      <el-table-column label="操作" width="160" fixed="right" v-if="adminStore.role==='SUPER_ADMIN'">
        <template #default="{row}"><div class="action-stack"><el-button size="small" @click="openEdit(row)">编辑</el-button><el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></div></template>
      </el-table-column>
    </el-table>
    </div>
    <el-dialog v-model="dialogVisible" :title="editing?'编辑管理员':'新增管理员'" width="500px">
      <el-form label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" :placeholder="editing?'留空不修改':''" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.role"><el-option value="ADMIN" label="普通管理员" /><el-option value="SUPER_ADMIN" label="超级管理员" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<style scoped>
.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }
.admin-cell { display: flex; align-items: center; gap: 12px; }
.admin-cell .avatar { width: 40px; height: 40px; border-radius: 8px; display: inline-flex; align-items: center; justify-content: center; color: #fff; background: linear-gradient(135deg, #182433, #ff6b35); font-weight: 800; }
.admin-cell div:last-child { display: grid; gap: 4px; }
.admin-cell span { color: #8a94a6; font-size: 12px; }
</style>
