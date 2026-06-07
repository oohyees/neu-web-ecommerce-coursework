<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import { adminLogin } from '@/api/auth'

const router = useRouter()
const adminStore = useAdminStore()
const form = ref({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) { ElMessage.warning('请输入账号和密码'); return }
  loading.value = true
  try {
    const res: any = await adminLogin(form.value)
    adminStore.setAuth(res.data)
    ElMessage.success('登录成功')
    router.replace('/dashboard')
  } catch { /* handled */ }
  finally { loading.value = false }
}
</script>
<template>
  <div class="login-page">
    <div class="login-copy">
      <span class="brand-pill">优品运营</span>
      <h1>商城管理后台</h1>
      <p>商品、订单、用户、内容和权限集中管理，支撑课堂演示的后台验收路径。</p>
    </div>
    <div class="login-card">
      <h2>优品商城 · 管理后台</h2>
      <el-form @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="form.username" placeholder="管理员账号" size="large" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password /></el-form-item>
        <el-form-item><el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%">登录</el-button></el-form-item>
      </el-form>
    </div>
  </div>
</template>
<style scoped>
.login-page { min-height: 100vh; display: grid; grid-template-columns: 1.1fr 420px; align-items: center; gap: 42px; padding: 56px 9vw; background: radial-gradient(circle at 18% 14%, rgba(255,107,53,.18), transparent 28%), linear-gradient(135deg, #172433 0%, #25384c 52%, #f5f7fa 52%); }
.login-copy { color: #fff; max-width: 560px; }
.brand-pill { display: inline-flex; padding: 7px 12px; border-radius: 999px; background: rgba(255,255,255,.12); color: #ffd4c3; font-size: 13px; font-weight: 800; }
.login-copy h1 { margin: 18px 0 12px; font-size: 44px; line-height: 1.1; letter-spacing: 0; }
.login-copy p { color: rgba(255,255,255,.72); font-size: 16px; line-height: 1.8; }
.login-card { width: 100%; padding: 40px; background: #fff; border: 1px solid rgba(17,24,39,.06); border-radius: 8px; box-shadow: 0 24px 60px rgba(17,24,39,.18); }
.login-card h2 { text-align: center; font-size: 20px; font-weight: 800; margin-bottom: 32px; color: #1f2d3d; }
@media (max-width: 900px) { .login-page { grid-template-columns: 1fr; padding: 32px 18px; background: linear-gradient(135deg, #172433 0%, #25384c 100%); } .login-copy h1 { font-size: 32px; } }
</style>
