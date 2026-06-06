<template>
  <main class="page">
    <el-card class="card" shadow="never">
      <h2>用户注册</h2>
      <p class="desc">创建你的商城账号，开始购物之旅</p>
      <el-input v-model="form.username" placeholder="用户名" size="large" />
      <el-input v-model="form.password" placeholder="密码" type="password" size="large" show-password />
      <el-input v-model="form.nickname" placeholder="昵称" size="large" />
      <el-input v-model="form.email" placeholder="邮箱" size="large" />
      <el-input v-model="form.code" placeholder="邮箱验证码" size="large">
        <template #append><el-button @click="sendCode" :loading="sending">发送验证码</el-button></template>
      </el-input>
      <el-input v-model="form.phone" placeholder="手机号（选填）" size="large" />
      <el-button type="danger" size="large" class="reg-btn" @click="register" :loading="loading">注 册</el-button>
      <div class="links">
        <el-button link @click="$router.push('/login')">已有账号？去登录</el-button>
        <el-button link @click="$router.push('/')">返回首页</el-button>
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
const form = ref({ username: '', password: '', nickname: '', email: '', phone: '', code: '' })
const sending = ref(false), loading = ref(false)
async function sendCode() {
  if (!form.value.email) return ElMessage.warning('请先输入邮箱')
  sending.value = true
  try {
    const { data } = await api.post('/auth/code', { email: form.value.email, purpose: 'REGISTER' })
    if (!data.success) return ElMessage.error(data.message)
    ElMessage.success('验证码已发送')
  } finally { sending.value = false }
}
async function register() {
  if (!form.value.username || !form.value.password || !form.value.email) return ElMessage.warning('请填写必填项')
  loading.value = true
  try {
    const { data } = await api.post('/auth/register/email', form.value)
    if (!data.success) return ElMessage.error(data.message)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch { ElMessage.error('注册失败') }
  finally { loading.value = false }
}
</script>
<style scoped>
.page { min-height: 100vh; display: grid; place-items: center; padding: 24px; background: linear-gradient(135deg, #fff, #fff1f2); }
.card { width: min(420px, 100%); border-radius: var(--radius-lg); }
.card :deep(.el-card__body) { display: grid; gap: 14px; padding: 28px; }
h2 { margin: 0; text-align: center; font-size: 22px; }
.desc { margin: 0; color: var(--muted); font-size: 14px; text-align: center; }
.card :deep(.el-input__wrapper) { border-radius: var(--radius); }
.reg-btn { width: 100%; height: 44px; font-size: 16px; font-weight: 700; border-radius: var(--radius); }
.links { display: flex; justify-content: center; gap: 8px; }
</style>
