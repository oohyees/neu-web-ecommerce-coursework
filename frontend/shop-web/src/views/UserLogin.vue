<template>
  <main class="auth-page">
    <section class="auth-hero">
      <div class="brand-lockup">
        <strong>EC</strong>
        <span>精选商城</span>
      </div>
      <h1>欢迎回来</h1>
      <p>登录你的账号，继续享受品质购物体验。</p>
      <ul class="feature-list">
        <li><span class="dot">✓</span> 正品保障 · 品质护航</li>
        <li><span class="dot">✓</span> 极速配送 · 当日发货</li>
        <li><span class="dot">✓</span> 售后无忧 · 退换便捷</li>
      </ul>
      <el-button link class="back-link" @click="$router.push('/')">← 返回商城首页</el-button>
    </section>
    <el-card class="auth-card" shadow="never">
      <h2>用户登录</h2>
      <p class="card-desc">欢迎回来，请登录你的账号</p>
      <el-form @submit.prevent="login">
        <el-input v-model="username" placeholder="请输入用户名" size="large" class="auth-input" />
        <el-input v-model="password" placeholder="请输入密码" type="password" size="large" class="auth-input" show-password @keyup.enter="login" />
      </el-form>
      <div class="row-options">
        <el-checkbox v-model="remember">记住密码</el-checkbox>
      </div>
      <el-button type="danger" size="large" class="auth-btn" :loading="loading" @click="login">登 录</el-button>
      <div class="links">
        <el-button link @click="$router.push('/register')">注册账号</el-button>
        <el-button link @click="$router.push('/forgot-password')">忘记密码</el-button>
        <el-button link @click="$router.push('/admin/login')">管理员登录</el-button>
      </div>
    </el-card>
  </main>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'
import { useUserStore, useCartStore, useFavoriteStore } from '@/stores'
const username = ref(localStorage.getItem('rememberUsername') || '')
const password = ref(localStorage.getItem('rememberPassword') || '')
const remember = ref(Boolean(localStorage.getItem('rememberUsername') && localStorage.getItem('rememberPassword')))
const loading = ref(false)
const router = useRouter()
const userStore = useUserStore()
async function login() {
  if (!username.value || !password.value) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    const { data } = await api.post('/auth/login', { username: username.value, password: password.value })
    if (!data.success) return ElMessage.error(data.message)
    userStore.setAuth(data.data)
    if (remember.value) {
      localStorage.setItem('rememberUsername', username.value)
      localStorage.setItem('rememberPassword', password.value)
    } else {
      localStorage.removeItem('rememberUsername')
      localStorage.removeItem('rememberPassword')
    }
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.auth-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 400px;
  gap: 40px;
  align-items: center;
  min-height: 100vh;
  padding: 48px 8vw;
  background: linear-gradient(135deg, #fff 0%, #fff1f2 100%);
}
.auth-hero {
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 480px;
  margin-left: auto;
}
.brand-lockup {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.brand-lockup strong {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  color: #fff;
  background: var(--brand);
  border-radius: 12px;
  font-size: 20px;
}
.brand-lockup span {
  font-size: 24px;
  font-weight: 800;
  color: var(--ink);
}
.auth-hero h1 {
  margin: 0 0 8px;
  font-size: 38px;
  font-weight: 800;
  color: var(--ink);
}
.auth-hero p {
  color: var(--muted);
  font-size: 16px;
  margin: 0 0 24px;
}
.feature-list {
  list-style: none;
  padding: 0;
  margin: 0 0 28px;
  display: grid;
  gap: 12px;
}
.feature-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: var(--ink);
}
.dot {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  color: #fff;
  background: var(--brand);
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}
.back-link {
  align-self: flex-start;
  font-size: 14px;
}
.auth-card {
  max-width: 400px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-lg);
}
.auth-card :deep(.el-card__body) {
  display: grid;
  gap: 16px;
  padding: 32px 28px;
}
.auth-card h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  text-align: center;
}
.card-desc {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
  text-align: center;
}
.auth-input :deep(.el-input__wrapper) {
  height: 44px;
  border-radius: var(--radius);
}
.row-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.auth-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 700;
  border-radius: var(--radius);
}
.links {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
}
.links :deep(.el-button) {
  font-size: 13px;
}
@media (max-width: 860px) {
  .auth-page {
    grid-template-columns: 1fr;
    padding: 32px 16px;
    gap: 28px;
  }
  .auth-hero {
    margin-left: 0;
    max-width: none;
    align-items: center;
    text-align: center;
  }
  .feature-list {
    justify-items: center;
  }
  .auth-card {
    max-width: none;
  }
  .auth-hero h1 {
    font-size: 28px;
  }
}
</style>
