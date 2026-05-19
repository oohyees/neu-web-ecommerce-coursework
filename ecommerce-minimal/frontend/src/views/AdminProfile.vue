<template>
  <AdminLayout>
    <AdminPageHeader title="个人资料" eyebrow="Profile" subtitle="管理管理员账户信息和登录密码。" />

    <div class="profile-grid">
      <section class="page-card">
        <h2>基本资料</h2>
        <el-form label-width="80px" class="profile-form">
          <el-form-item label="昵称">
            <el-input v-model="profile.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profile.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profile.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item>
            <el-button type="danger" @click="saveProfile" :loading="savingProfile">保存资料</el-button>
          </el-form-item>
        </el-form>
      </section>

      <section class="page-card">
        <h2>修改密码</h2>
        <el-form label-width="88px" class="profile-form">
          <el-form-item label="原密码">
            <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="danger" @click="save" :loading="savingPwd">修改密码</el-button>
          </el-form-item>
        </el-form>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useSessionStore } from '../store'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminPageHeader from '../components/AdminPageHeader.vue'

const session = useSessionStore()
const form = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const profile = ref({})
const savingProfile = ref(false)
const savingPwd = ref(false)

async function save() {
  if (!form.value.oldPassword || !form.value.newPassword) return ElMessage.warning('请填写完整密码信息')
  if (form.value.newPassword !== form.value.confirmPassword) return ElMessage.warning('两次输入的新密码不一致')
  savingPwd.value = true
  try {
    const { data } = await api.put('/auth/admin/password', { adminId: session.adminId, oldPassword: form.value.oldPassword, newPassword: form.value.newPassword })
    if (!data.success) return ElMessage.error(data.message)
    ElMessage.success('密码已更新')
    form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch { ElMessage.error('修改密码失败') }
  finally { savingPwd.value = false }
}

async function saveProfile() {
  savingProfile.value = true
  try {
    await api.put('/auth/admin/profile', { adminId: session.adminId, ...profile.value })
    ElMessage.success('资料已保存')
  } catch { ElMessage.error('保存失败') }
  finally { savingProfile.value = false }
}

onMounted(async () => {
  profile.value = (await api.get('/auth/admin/profile', { params: { adminId: session.adminId } })).data.data || {}
})
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.page-card {
  padding: 24px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
}

.page-card h2 {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 700;
}

.profile-form {
  max-width: 400px;
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
