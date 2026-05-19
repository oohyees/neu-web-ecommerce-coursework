<template>
  <main class="admin-auth">
    <el-card class="card" shadow="never">
      <div class="brand-lockup">
        <strong>EC</strong>
        <span>商城运营后台</span>
      </div>
      <h1>管理员登录</h1>
      <p class="desc">请输入管理员账号密码登录后台管理系统</p>
      <el-input v-model="username" placeholder="用户名" size="large" class="auth-input" />
      <el-input v-model="password" placeholder="密码" type="password" size="large" class="auth-input" show-password />
      <el-button type="danger" size="large" class="auth-btn" @click="login" :loading="loading">登录后台</el-button>
      <el-button link @click="$router.push('/')">← 返回商城首页</el-button>
    </el-card>
  </main>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useSessionStore } from '../store'
const username = ref('admin'), password = ref('admin123'), loading = ref(false)
const router = useRouter(), session = useSessionStore()
async function login() {
  if (!username.value || !password.value) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    const { data } = await api.post('/auth/admin/login', { username: username.value, password: password.value })
    if (!data.success) return ElMessage.error(data.message)
    session.setAdmin(data.data)
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch {
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally { loading.value = false }
}
</script>
<style scoped>
.admin-auth {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #111827 0%, #1f2937 100%);
}
.card {
  width: min(400px, 100%);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, .1);
  background: #fff;
}
.card :deep(.el-card__body) {
  display: grid;
  gap: 16px;
  padding: 32px 28px;
}
.brand-lockup {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
}
.brand-lockup strong {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  color: #fff;
  background: var(--brand);
  border-radius: 10px;
  font-size: 16px;
}
.brand-lockup span {
  font-size: 18px;
  font-weight: 800;
  color: var(--ink);
}
h1 { margin: 0; font-size: 22px; text-align: center; }
.desc { margin: 0; color: var(--muted); font-size: 14px; text-align: center; }
.auth-input :deep(.el-input__wrapper) { height: 44px; border-radius: var(--radius); }
.auth-btn { width: 100%; height: 44px; font-size: 16px; font-weight: 700; border-radius: var(--radius); }
</style>
