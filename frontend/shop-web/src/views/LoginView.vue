<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const res: any = await login(form.value)
    userStore.setAuth(res.data)
    ElMessage.success(`欢迎回来，${res.data.nickname || form.value.username}`)
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch { /* interceptor handles */ }
  finally { loading.value = false }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>登录</h2>
      <p class="auth-subtitle">欢迎回到优品商城</p>
      <el-form @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" size="large" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password /></el-form-item>
        <el-form-item><el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%">登录</el-button></el-form-item>
      </el-form>
      <div class="auth-links">
        <router-link to="/register">还没有账号？立即注册</router-link>
        <router-link to="/forgot-password">忘记密码？</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-container { min-height: 80vh; display: flex; align-items: center; justify-content: center; padding: 40px 16px; }
.auth-card { width: 100%; max-width: 400px; padding: 40px; background: #fff; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }
.auth-card h2 { font-size: 24px; font-weight: 700; text-align: center; margin-bottom: 4px; }
.auth-subtitle { text-align: center; color: #999; font-size: 14px; margin-bottom: 28px; }
.auth-links { display: flex; justify-content: space-between; font-size: 13px; margin-top: 12px; }
.auth-links a { color: var(--color-primary); }
</style>
