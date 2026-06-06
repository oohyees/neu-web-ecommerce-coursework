<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import { fetchAdminProfile, updateAdminProfile, changeAdminPassword } from '@/api/auth'

const adminStore = useAdminStore()
const profile = ref({ nickname: '', email: '', phone: '' })
const pwForm = ref({ oldPassword: '', newPassword: '' })

async function load() {
  try { const res: any = await fetchAdminProfile(); const d = res.data ?? res; profile.value = { nickname: d.nickname || '', email: d.email || '', phone: d.phone || '' } } catch { /* handled */ }
}
async function saveProfile() { try { await updateAdminProfile(profile.value); ElMessage.success('已保存') } catch { /* handled */ } }
async function savePassword() {
  if (!pwForm.value.oldPassword || !pwForm.value.newPassword) { ElMessage.warning('请填写完整'); return }
  try { await changeAdminPassword(pwForm.value); ElMessage.success('密码已修改，请重新登录'); adminStore.logout() } catch { /* handled */ }
}
onMounted(load)
</script>
<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <div class="card">
      <h3>基本资料</h3>
      <el-form label-width="80px"><el-form-item label="昵称"><el-input v-model="profile.nickname" /></el-form-item><el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item><el-form-item label="手机"><el-input v-model="profile.phone" /></el-form-item><el-form-item><el-button type="primary" @click="saveProfile">保存</el-button></el-form-item></el-form>
    </div>
    <div class="card">
      <h3>修改密码</h3>
      <el-form label-width="80px"><el-form-item label="原密码"><el-input v-model="pwForm.oldPassword" type="password" show-password /></el-form-item><el-form-item label="新密码"><el-input v-model="pwForm.newPassword" type="password" show-password /></el-form-item><el-form-item><el-button type="primary" @click="savePassword">修改密码</el-button></el-form-item></el-form>
    </div>
  </div>
</template>
<style scoped>
.profile-page { max-width: 500px; }
.profile-page h2 { font-size: 20px; font-weight: 700; margin-bottom: 20px; }
.card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.card h3 { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
</style>
