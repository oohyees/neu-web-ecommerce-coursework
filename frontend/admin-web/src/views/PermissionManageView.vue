<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
onMounted(load)
</script>
<template>
  <div>
    <div class="tb-header"><h2>权限管理</h2><el-button v-if="adminStore.role==='SUPER_ADMIN'" type="primary" @click="openAdd">新增管理员</el-button></div>
    <el-table :data="admins" stripe>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="role" label="角色" width="120"><template #default="{row}"><el-tag :type="row.role==='SUPER_ADMIN'?'danger':'info'" size="small">{{row.role}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="160" v-if="adminStore.role==='SUPER_ADMIN'">
        <template #default="{row}"><el-button text size="small" @click="openEdit(row)">编辑</el-button><el-button text size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template>
      </el-table-column>
    </el-table>
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
<style scoped>.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; } .tb-header h2 { font-size: 20px; font-weight: 700; }</style>
