<template>
  <div class="shop-shell">
    <div class="topbar">
      <div class="page-wrap topbar-inner">
        <span>欢迎来到精选商城</span>
        <nav>
          <router-link to="/orders">我的订单</router-link>
          <router-link to="/user/favorites">我的收藏</router-link>
          <router-link to="/consultations">客户服务</router-link>
          <router-link to="/admin/login">商家后台</router-link>
          <button v-if="session.userId" @click="logout">退出</button>
          <router-link v-else to="/login">登录/注册</router-link>
        </nav>
      </div>
    </div>
    <header class="masthead">
      <div class="page-wrap masthead-inner">
        <div class="brand" @click="$router.push('/')">
          <strong>EC</strong>
          <span>精选商城</span>
        </div>
        <div class="search">
          <el-input class="search-input" v-model="keyword" size="large" placeholder="搜索商品、分类、活动" @keyup.enter="search">
            <template #append><el-button @click="search">搜索</el-button></template>
          </el-input>
          <div class="hotwords">
            <button v-for="word in hotwords" :key="word" @click="quickSearch(word)">{{ word }}</button>
          </div>
        </div>
        <el-badge :value="cartCount" :hidden="!cartCount" :max="99">
          <el-button class="cart-btn" size="large" @click="$router.push('/cart')">
            <span class="cart-icon">🛒</span> 购物车
          </el-button>
        </el-badge>
      </div>
    </header>
    <nav class="navline">
      <div class="page-wrap navline-inner">
        <router-link to="/">首页</router-link>
        <router-link to="/products">全部商品</router-link>
        <router-link to="/orders">订单中心</router-link>
        <router-link to="/user/profile">个人中心</router-link>
        <router-link to="/feedback">意见反馈</router-link>
      </div>
    </nav>
    <main class="main"><slot /></main>
    <footer class="footer">
      <div class="page-wrap footer-grid">
        <div>
          <strong>品质保障</strong>
          <span>真实商品数据，完整购物闭环</span>
        </div>
        <div>
          <strong>便捷配送</strong>
          <span>订单状态与物流轨迹可查</span>
        </div>
        <div>
          <strong>售后服务</strong>
          <span>反馈、咨询、评价全流程支持</span>
        </div>
      </div>
      <p class="footer-copy">&copy; 2026 精选商城 · Web开发技术课程大作业</p>
    </footer>
  </div>
</template>
<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useSessionStore } from '../store'
import { api } from '../api'
const router = useRouter(), session = useSessionStore()
const keyword = ref('')
const cartCount = ref(0)
const hotwords = ['键盘', '鼠标', 'SSD', '耳机']
function search() { router.push({ path: '/products', query: { keyword: keyword.value } }) }
function quickSearch(word) { keyword.value = word; search() }
async function fetchCartCount() {
  if (!session.userId) { cartCount.value = 0; return }
  try {
    const items = (await api.get('/cart', { params: { userId: session.userId } })).data.data || []
    cartCount.value = items.length
  } catch { cartCount.value = 0 }
}
async function logout() {
  try { await api.post('/auth/logout', { token: localStorage.getItem('token') || '' }) } catch {}
  session.logout(); router.push('/login')
}
watch(() => session.userId, () => { fetchCartCount() })
onMounted(() => { fetchCartCount() })
</script>
<style scoped>
.shop-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--soft);
}

/* ---- top bar ---- */
.topbar {
  font-size: 13px;
  color: var(--muted);
  background: #fff;
  border-bottom: 1px solid var(--line);
}
.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 36px;
}
.topbar nav {
  display: flex;
  gap: 18px;
  align-items: center;
}
.topbar nav a {
  color: var(--muted);
  transition: color .2s;
}
.topbar nav a:hover {
  color: var(--brand);
}
.topbar button {
  padding: 0;
  color: var(--muted);
  cursor: pointer;
  background: none;
  border: 0;
}
.topbar button:hover {
  color: var(--brand);
}

/* ---- masthead ---- */
.masthead {
  background: #fff;
  border-bottom: 1px solid var(--line);
}
.masthead-inner {
  display: flex;
  gap: 28px;
  align-items: center;
  min-height: 92px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  min-width: 190px;
}
.brand strong {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  color: #fff;
  background: var(--brand);
  border-radius: 10px;
  font-size: 18px;
}
.brand span {
  font-size: 22px;
  font-weight: 800;
  color: var(--ink);
}
.search {
  flex: 1;
}
.search-input :deep(.el-input__wrapper) {
  border-radius: 999px 0 0 999px;
  box-shadow: 0 0 0 1px var(--line) inset;
}
.search-input :deep(.el-input-group__append) {
  overflow: hidden;
  background: var(--brand);
  border-color: var(--brand);
  border-radius: 0 999px 999px 0;
  box-shadow: none;
}
.search-input :deep(.el-input-group__append .el-button) {
  padding: 0 20px;
  color: #fff;
  background: var(--brand);
  border-color: var(--brand);
  font-weight: 600;
}
.hotwords {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}
.hotwords button {
  padding: 0;
  color: var(--muted);
  cursor: pointer;
  background: none;
  border: 0;
  font-size: 13px;
  transition: color .2s;
}
.hotwords button:hover {
  color: var(--brand);
}
.cart-btn {
  min-width: 120px;
  background: #fff;
  border: 1px solid var(--line);
  color: var(--ink);
  font-weight: 600;
}
.cart-btn:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.cart-icon {
  margin-right: 4px;
}

/* ---- nav line ---- */
.navline {
  background: #fff;
  border-bottom: 1px solid var(--line);
}
.navline-inner {
  display: flex;
  gap: 32px;
  height: 48px;
}
.navline a {
  display: flex;
  align-items: center;
  height: 100%;
  color: var(--ink);
  font-weight: 650;
  font-size: 15px;
  border-bottom: 2px solid transparent;
  transition: color .2s, border-color .2s;
}
.navline a.router-link-active,
.navline a:hover {
  color: var(--brand);
  border-bottom-color: var(--brand);
}

/* ---- main ---- */
.main {
  flex: 1;
  padding: 22px 16px 34px;
}

/* ---- footer ---- */
.footer {
  padding: 28px 16px 20px;
  background: #fff;
  border-top: 1px solid var(--line);
}
.footer-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.footer-grid div {
  display: grid;
  gap: 6px;
}
.footer-grid strong {
  font-size: 15px;
}
.footer-grid span {
  color: var(--muted);
  font-size: 13px;
}
.footer-copy {
  margin: 20px 0 0;
  color: var(--muted);
  text-align: center;
  font-size: 13px;
}

@media (max-width: 768px) {
  .topbar-inner {
    align-items: flex-start;
    height: auto;
    padding: 8px 12px;
    flex-direction: column;
    gap: 6px;
  }
  .topbar nav {
    flex-wrap: wrap;
    gap: 12px;
  }
  .masthead-inner {
    align-items: stretch;
    padding: 14px 12px;
    flex-direction: column;
    gap: 14px;
  }
  .brand {
    min-width: 0;
  }
  .navline-inner {
    gap: 0;
    overflow-x: auto;
  }
  .navline a {
    flex: 0 0 auto;
    padding: 0 14px;
  }
  .footer-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .main {
    padding: 16px 12px 28px;
  }
}
</style>
