<template>
  <main class="page">
    <el-card class="card" shadow="never">
      <h2>找回密码</h2>
      <p class="desc">输入注册邮箱获取验证码，然后设置新密码。</p>
      <el-input v-model="form.email" placeholder="注册邮箱" size="large" />
      <el-input v-model="form.code" placeholder="邮箱验证码" size="large">
        <template #append><el-button @click="sendCode" :loading="sending">发送验证码</el-button></template>
      </el-input>
      <el-input v-model="form.password" type="password" placeholder="新密码" size="large" show-password />
      <el-button type="danger" size="large" class="reset-btn" @click="reset" :loading="loading">重置密码</el-button>
      <div class="links">
        <el-button link @click="$router.push('/login')">返回登录</el-button>
      </div>
    </el-card>
  </main>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
const router = useRouter()
const form = ref({ email: '', code: '', password: '' })
const sending = ref(false), loading = ref(false)
async function sendCode() {
  if (!form.value.email) return ElMessage.warning('请输入邮箱')
  sending.value = true
  try {
    const { data } = await api.post('/auth/code', { email: form.value.email, purpose: 'RESET' })
    if (!data.success) return ElMessage.error(data.message)
    ElMessage.success('验证码已发送')
  } finally { sending.value = false }
}
async function reset() {
  if (!form.value.email || !form.value.code || !form.value.password) return ElMessage.warning('请填写完整信息')
  loading.value = true
  try {
    const { data } = await api.post('/auth/password/reset', form.value)
    if (!data.success) return ElMessage.error(data.message)
    ElMessage.success('密码已重置，请重新登录')
    router.push('/login')
  } catch { ElMessage.error('重置失败') }
  finally { loading.value = false }
}
</script>
<style scoped>
.page { min-height: 100vh; display: grid; place-items: center; padding: 24px; background: linear-gradient(135deg, #fff, #fff1f2); }
.card { width: min(400px, 100%); border-radius: var(--radius-lg); }
.card :deep(.el-card__body) { display: grid; gap: 14px; padding: 28px; }
h2 { margin: 0; text-align: center; font-size: 22px; }
.desc { margin: 0; color: var(--muted); font-size: 14px; text-align: center; }
.card :deep(.el-input__wrapper) { border-radius: var(--radius); }
.reset-btn { width: 100%; height: 44px; font-size: 16px; font-weight: 700; border-radius: var(--radius); }
.links { display: flex; justify-content: center; }
</style>
