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
  <div class="admin-page profile-page">
    <div class="tb-header">
      <div><h2>个人中心</h2><p class="page-subtitle">维护管理员资料和密码，演示后台账号自管理能力。</p></div>
    </div>
    <div class="profile-grid">
    <div class="card">
      <h3>基本资料</h3>
      <el-form label-width="80px"><el-form-item label="昵称"><el-input v-model="profile.nickname" /></el-form-item><el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item><el-form-item label="手机"><el-input v-model="profile.phone" /></el-form-item><el-form-item><el-button type="primary" @click="saveProfile">保存</el-button></el-form-item></el-form>
    </div>
    <div class="card">
      <h3>修改密码</h3>
      <el-form label-width="80px"><el-form-item label="原密码"><el-input v-model="pwForm.oldPassword" type="password" show-password /></el-form-item><el-form-item label="新密码"><el-input v-model="pwForm.newPassword" type="password" show-password /></el-form-item><el-form-item><el-button type="primary" @click="savePassword">修改密码</el-button></el-form-item></el-form>
    </div>
    </div>
  </div>
</template>
<style scoped>
.page-subtitle { margin-top: 8px; color: #6b7280; font-size: 13px; }
.profile-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.card { background: #fff; border: 1px solid var(--color-line); border-radius: 8px; padding: 24px; box-shadow: var(--shadow-card); }
.card h3 { font-size: 16px; font-weight: 800; margin-bottom: 16px; }
@media (max-width: 900px) { .profile-grid { grid-template-columns: 1fr; } }
</style>
