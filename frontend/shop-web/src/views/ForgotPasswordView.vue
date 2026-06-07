<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendCode, resetPassword } from '@/api/user'

const router = useRouter()
const form = ref({ email: '', code: '', password: '' })
const sending = ref(false)
const loading = ref(false)
const countdown = ref(0)

async function handleSendCode() {
  if (!form.value.email) { ElMessage.warning('请输入邮箱'); return }
  sending.value = true
  try {
    await sendCode({ email: form.value.email, purpose: 'RESET' })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const t = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(t) }, 1000)
  } catch { /* handled */ }
  finally { sending.value = false }
}

async function handleReset() {
  if (!form.value.email || !form.value.code || !form.value.password) {
    ElMessage.warning('请填写完整信息'); return
  }
  loading.value = true
  try {
    await resetPassword(form.value)
    ElMessage.success('密码已重置，请登录')
    router.push('/login')
  } catch { /* handled */ }
  finally { loading.value = false }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>找回密码</h2>
      <p class="auth-subtitle">通过注册邮箱重置密码</p>
      <div class="auth-benefits"><span>真实邮件</span><span>验证码校验</span><span>重置后登录</span></div>
      <el-form @submit.prevent="handleReset">
        <el-form-item><el-input v-model="form.email" placeholder="注册邮箱" size="large" /></el-form-item>
        <el-form-item>
          <div style="display:flex;gap:10px;width:100%">
            <el-input v-model="form.code" placeholder="验证码" size="large" style="flex:1" />
            <el-button size="large" :disabled="countdown>0" :loading="sending" @click="handleSendCode">{{ countdown>0 ? `${countdown}s` : '获取验证码' }}</el-button>
          </div>
        </el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="新密码" size="large" show-password /></el-form-item>
        <el-form-item><el-button type="primary" size="large" :loading="loading" @click="handleReset" style="width:100%">重置密码</el-button></el-form-item>
      </el-form>
      <div class="auth-links"><router-link to="/login">返回登录</router-link></div>
    </div>
  </div>
</template>

<style scoped>
.auth-container { min-height: 80vh; display: flex; align-items: center; justify-content: center; padding: 40px 16px; }
.auth-card { width: 100%; max-width: 400px; padding: 40px; background: #fff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }
.auth-card h2 { font-size: 24px; font-weight: 700; text-align: center; margin-bottom: 4px; }
.auth-subtitle { text-align: center; color: #999; font-size: 14px; margin-bottom: 28px; }
.auth-benefits { display: flex; justify-content: center; gap: 8px; margin: -12px 0 22px; flex-wrap: wrap; }
.auth-benefits span { padding: 5px 9px; border-radius: 999px; background: #fff5f0; color: var(--color-primary); font-size: 12px; font-weight: 700; }
.auth-links { text-align: center; font-size: 13px; margin-top: 12px; }
.auth-links a { color: var(--color-primary); }
</style>
