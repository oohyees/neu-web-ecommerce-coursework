<template>
  <section class="page-card">
    <h2 class="card-title">修改密码</h2>
    <el-form label-width="88px" class="security-form">
      <el-form-item label="原密码">
        <el-input v-model="pwd.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="pwd.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="pwd.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="changePassword">修改密码</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useSessionStore } from '../store'

const session = useSessionStore()
const pwd = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

async function changePassword() {
  if (!pwd.value.oldPassword || !pwd.value.newPassword) return ElMessage.warning('请填写完整密码信息')
  if (pwd.value.newPassword !== pwd.value.confirmPassword) return ElMessage.warning('两次输入的新密码不一致')
  const { data } = await api.put('/auth/password', { userId: session.userId, oldPassword: pwd.value.oldPassword, newPassword: pwd.value.newPassword })
  if (!data.success) return ElMessage.error(data.message)
  ElMessage.success('密码已更新')
  pwd.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
}
</script>

<style scoped>
.card-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 700;
}

.security-form {
  max-width: 420px;
}
</style>
