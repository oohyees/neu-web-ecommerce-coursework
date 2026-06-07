<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile, changePassword, uploadAvatar } from '@/api/user'

const userStore = useUserStore()
const profile = ref({ nickname: '', email: '', phone: '', avatarUrl: '' })
const pwForm = ref({ oldPassword: '', newPassword: '' })
const saving = ref(false)
const uploading = ref(false)

async function load() {
  try {
    const res: any = await getProfile()
    const d = res.data ?? res
    profile.value = { nickname: d.nickname || '', email: d.email || '', phone: d.phone || '', avatarUrl: d.avatarUrl || '' }
    userStore.setProfile({ nickname: d.nickname, avatarUrl: d.avatarUrl, email: d.email, phone: d.phone })
  } catch { /* handled */ }
}

async function saveProfile() {
  saving.value = true
  try {
    await updateProfile(profile.value)
    userStore.setProfile(profile.value)
    ElMessage.success('已保存')
  } catch { /* handled */ }
  finally { saving.value = false }
}

async function savePassword() {
  if (!pwForm.value.oldPassword || !pwForm.value.newPassword) { ElMessage.warning('请填写完整'); return }
  saving.value = true
  try {
    await changePassword(pwForm.value)
    ElMessage.success('密码已修改，请重新登录')
    userStore.logout()
  } catch { /* handled */ }
  finally { saving.value = false }
}

onMounted(load)

async function handleAvatarUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res: any = await uploadAvatar(file)
    const url = res.data ?? res
    profile.value.avatarUrl = url
    userStore.setProfile({ avatarUrl: url })
    ElMessage.success('头像上传成功')
  } catch { ElMessage.error('上传失败') }
  finally { uploading.value = false; (e.target as HTMLInputElement).value = '' }
}
</script>

<template>
  <div class="page-container profile-page">
    <div class="page-intro">
      <div>
        <h2 class="page-title">个人中心</h2>
        <p>维护昵称、邮箱、手机和头像信息，并提供账户安全入口。</p>
      </div>
    </div>
    <div class="profile-card">
      <h3>基本资料</h3>
      <el-form label-width="80px">
        <el-form-item label="头像">
          <div class="avatar-row">
            <div v-if="profile.avatarUrl" class="avatar-preview">
              <img :src="profile.avatarUrl" alt="头像" />
            </div>
            <div v-else class="avatar-preview avatar-empty">未设置</div>
            <div class="avatar-actions">
              <el-input v-model="profile.avatarUrl" placeholder="图片URL" style="flex:1" />
              <label class="upload-btn"><span :class="{ 'is-loading': uploading }">{{ uploading ? '上传中...' : '上传图片' }}</span><input type="file" accept="image/*" hidden @change="handleAvatarUpload" :disabled="uploading" /></label>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="昵称"><el-input v-model="profile.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="profile.phone" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button></el-form-item>
      </el-form>
    </div>
    <div class="profile-card">
      <h3>修改密码</h3>
      <el-form label-width="80px">
        <el-form-item label="原密码"><el-input v-model="pwForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="savePassword">修改密码</el-button></el-form-item>
      </el-form>
    </div>
  </div>
</template>
<style scoped>
.profile-page { max-width: 500px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 24px; }
.profile-card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.profile-card h3 { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.avatar-row { display: flex; align-items: center; gap: 16px; width: 100%; }
.avatar-preview { width: 64px; height: 64px; border-radius: 50%; overflow: hidden; flex-shrink: 0; border: 2px solid #f0f0f0; }
.avatar-preview img { width: 100%; height: 100%; object-fit: cover; }
.avatar-empty { display: flex; align-items: center; justify-content: center; background: #f5f5f5; color: #bbb; font-size: 12px; }
.avatar-actions { display: flex; gap: 8px; flex: 1; }
.upload-btn { display:inline-flex;align-items:center;padding:0 15px;border:1px solid var(--color-primary);border-radius:4px;color:var(--color-primary);cursor:pointer;font-size:13px;white-space:nowrap;transition:all .2s; }
.upload-btn:hover { background:var(--color-primary);color:#fff; }
.upload-btn .is-loading { opacity:.6; }
</style>
