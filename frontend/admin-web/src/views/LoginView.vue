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
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f5f7fa; }
.login-card { width: 380px; padding: 40px; background: #fff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
.login-card h2 { text-align: center; font-size: 20px; font-weight: 700; margin-bottom: 32px; color: #1f2d3d; }
</style>
