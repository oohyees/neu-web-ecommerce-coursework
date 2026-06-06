# 前端重建方案（终版）

> 制定日期：2026-06-06
> 基准样本：李旋（满分验证） + 张臻（TS严格度） + commerce-web（API-First） + 代宇霆（薄组件） + 杨浩伟（WebSocket）
> 后端契约：`docs/api-reference.md` 70+ 端点，71项测试全通过，8组冒烟全PASS

---

## 零、为什么旧代码该死

旧前端数据流方向是反的：

```
旧：先画 UI → 硬造 mock 数据 → 后接 API → 发现字段对不上 → 到处补丁
新：API 文档 → TypeScript 类型 → API 层 → Store → Router → 最后才写 UI
```

后端 71 个测试已经锁定 API 契约是铁定的。前端唯一的真相源就是 API 响应结构。不承认这一点就会再次返工。

---

## 一、架构决策：两个独立 Vite 项目

**这是老师课上的硬性要求——"两个业务"。** 李旋的方案已验证满分。

```
frontend/
├── shop-web/          # 客户端，端口 5173，docker 映射 18095
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json / tsconfig.app.json / tsconfig.node.json
│   ├── index.html
│   └── src/
│       ├── main.ts                    # createApp → pinia → router → element-plus
│       ├── App.vue                    # 按 route.meta 切换布局
│       ├── api/                       # 10 个模块
│       │   ├── request.ts             #   Axios 实例 + 拦截器
│       │   ├── user.ts                #   login / register / code / profile / password
│       │   ├── product.ts             #   list / detail / categories / reviews / favorites
│       │   ├── cart.ts
│       │   ├── order.ts
│       │   ├── address.ts
│       │   ├── coupon.ts
│       │   ├── promotion.ts           #   promotions / seckill / specs
│       │   ├── notice.ts              #   announcements / activity-notices / home
│       │   ├── cs.ts                  #   consultations / WebSocket chat
│       │   └── feedback.ts
│       ├── stores/                    # 3 个 Pinia store
│       │   ├── user.ts                #   token / userId / nickname / avatar / isLoggedIn
│       │   ├── cart.ts                #   itemCount / refresh()
│       │   └── favorite.ts            #   ids: Set / toggle() / refresh()
│       ├── router/
│       │   └── index.ts               #   20 条路由 + auth/guest 守卫
│       ├── views/                     # 20 个页面
│       │   ├── HomeView.vue
│       │   ├── LoginView.vue
│       │   ├── RegisterView.vue
│       │   ├── ForgotPasswordView.vue
│       │   ├── CategoryView.vue       #   /category/:id  分类商品列表
│       │   ├── SearchView.vue         #   /search        关键词搜索结果
│       │   ├── ProductDetailView.vue
│       │   ├── CartView.vue
│       │   ├── CheckoutView.vue
│       │   ├── PaymentView.vue
│       │   ├── OrdersView.vue
│       │   ├── OrderDetailView.vue
│       │   ├── ProfileView.vue
│       │   ├── AddressView.vue
│       │   ├── FavoritesView.vue
│       │   ├── CouponView.vue
│       │   ├── SeckillView.vue
│       │   ├── NoticeListView.vue
│       │   ├── FeedbackView.vue
│       │   └── CustomerServiceView.vue
│       ├── components/                # 按功能域分组
│       │   ├── layout/
│       │   │   ├── AppHeader.vue      #   全局顶栏（logo + 搜索 + 导航 + 购物车图标 + 用户菜单）
│       │   │   ├── HomeSearchBar.vue  #   首页大搜索框
│       │   │   ├── ShopNavLinks.vue   #   分类导航链接
│       │   │   └── RightSideBar.vue   #   右侧固定工具栏
│       │   ├── home/
│       │   │   ├── CategorySidebar.vue
│       │   │   ├── ProductPromoGrid.vue
│       │   │   └── ProductScrollSection.vue
│       │   ├── product/
│       │   │   └── ProductCard.vue
│       │   ├── notice/
│       │   │   └── ActivityNoticeBanner.vue
│       │   └── auth/
│       │       └── AuthLayout.vue     #   登录/注册/找回密码的居中卡片布局
│       ├── utils/
│       │   ├── freshLogin.ts          #   标记"刚刚登录"，触发 cart/favorite 刷新
│       │   ├── image.ts               #   图片 URL 处理（兜底图等）
│       │   └── promotionTime.ts       #   促销时间格式化
│       ├── constants/
│       │   └── shopNav.ts             #   导航菜单配置
│       └── styles/
│           └── global.css             #   CSS 变量 + 全局样式（约150行）
│
└── admin-web/         # 管理后台，端口 5174，docker 映射 18082
    ├── package.json
    ├── vite.config.ts
    ├── tsconfig.json / tsconfig.app.json / tsconfig.node.json
    ├── index.html
    └── src/
        ├── main.ts                    # createApp → pinia → permission-directive → router → element-plus
        ├── App.vue                    # 桌面侧边栏布局 + 移动端抽屉布局
        ├── api/                       # 12 个模块
        │   ├── request.ts             #   同 shop-web 模式
        │   ├── auth.ts                #   admin login / logout / me
        │   ├── dashboard.ts           #   summary / export
        │   ├── product.ts             #   admin CRUD / import / export / categories
        │   ├── order.ts               #   admin list / detail / ship / refund / status / export
        │   ├── user.ts                #   admin user CRUD / enable-disable
        │   ├── promotion.ts           #   admin promotions / coupons CRUD
        │   ├── banner.ts              #   admin banners CRUD
        │   ├── notice.ts              #   admin announcements / activity-notices CRUD
        │   ├── feedback.ts            #   admin feedback list / reply / mark-processed
        │   ├── cs.ts                  #   admin consultations list / reply
        │   ├── permissionManage.ts    #   RBAC: admins CRUD + roles + permissions
        │   └── profile.ts             #   admin profile / password
        ├── stores/
        │   └── admin.ts               #   token / adminId / username / role / permissions[] / hasPermission()
        ├── router/
        │   └── index.ts               #   16 条路由 + auth + permission + superAdminOnly 守卫
        ├── views/                     # 16 个页面
        │   ├── LoginView.vue
        │   ├── DashboardView.vue
        │   ├── UsersView.vue
        │   ├── CategoriesView.vue
        │   ├── ProductsView.vue
        │   ├── PromotionsView.vue     #   含秒杀和优惠券子 tab
        │   ├── ReviewsView.vue
        │   ├── OrdersView.vue
        │   ├── BannersView.vue
        │   ├── NoticesView.vue        #   含公告和活动通知子 tab
        │   ├── FeedbacksView.vue
        │   ├── CsView.vue
        │   ├── PermissionManageView.vue  # 含管理员列表 / 角色 / 权限树三个子 tab
        │   └── AdminProfileView.vue
        ├── components/
        │   ├── layout/
        │   │   └── AdminSidebar.vue   #   侧边导航菜单（桌面）+ 抽屉内容（移动）
        │   ├── product/
        │   │   ├── CategoryPicker.vue
        │   │   └── ProductParamsEditor.vue
        │   └── PageHeader.vue         #   页面标题 + 面包屑 + 操作按钮
        ├── directives/
        │   └── permission.ts          #   v-permission 指令，按钮级权限控制
        ├── utils/
        │   └── productParams.ts       #   商品参数解析/序列化
        └── styles/
            └── global.css
```

---

## 二、技术栈：极简克制

### shop-web 依赖

```json
{
  "dependencies": {
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.7.9",
    "element-plus": "^2.9.1",
    "pinia": "^2.3.0",
    "pinia-plugin-persistedstate": "^3.2.1",
    "vue": "^3.5.13",
    "vue-router": "^4.5.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2.1",
    "typescript": "~5.6.2",
    "vite": "^6.0.5",
    "vue-tsc": "^2.2.0"
  }
}
```

### admin-web 依赖

```json
{
  "dependencies": {
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.7.9",
    "echarts": "^6.1.0",
    "element-plus": "^2.9.1",
    "pinia": "^2.3.0",
    "pinia-plugin-persistedstate": "^3.2.1",
    "vue": "^3.5.13",
    "vue-router": "^4.5.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2.1",
    "typescript": "~5.6.2",
    "vite": "^6.0.5",
    "vue-tsc": "^2.2.0"
  }
}
```

### 故意不装的库

| 不装 | 原因 |
|------|------|
| Tailwind CSS | 李旋用 150 行 CSS 自定义属性就拿了满分。Tailwind 增加依赖和构建复杂度，对评分无增量收益 |
| vue-echarts | 直接用 ECharts 的 `init()` / `setOption()`，少一层封装 |
| pinia-plugin-persistedstate（admin-web 可选） | admin store 本身就小，localStorage 手动读写也可以 |
| Vitest / Playwright | 本次不做前端测试。后端 71 个测试 + 8 组冒烟已经验证了 API，前端对接后用浏览器人工验证即可 |
| any icon library | `@element-plus/icons-vue` 已够用 |

---

## 三、核心模块设计（逐文件说明）

### 3.1 API 层 — 借鉴 commerce-web 的类型优先

每个 API 文件的结构：**类型定义 → 函数导出**。类型从你们的 `docs/api-reference.md` 推导。

```typescript
// api/user.ts 示例

import request from './request'

// ═══ 类型定义（来自 API 文档） ═══
export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userId: number
  nickname: string
}

export interface SendCodeParams {
  email: string
  purpose: 'REGISTER' | 'RESET'
}

// ═══ API 函数 ═══
export function login(data: LoginParams) {
  return request.post<{ success: boolean; data: LoginResult }>('/auth/login', data)
}

export function sendCode(data: SendCodeParams) {
  return request.post('/auth/code', data)
}

export function registerByEmail(data: {
  username: string; password: string; nickname: string;
  email: string; phone: string; code: string
}) {
  return request.post('/auth/register/email', data)
}

export function resetPassword(data: { email: string; code: string; password: string }) {
  return request.post('/auth/password/reset', data)
}

export function getProfile() {
  return request.get('/auth/profile')
}

export function updateProfile(data: { nickname?: string; avatarUrl?: string; email?: string; phone?: string }) {
  return request.put('/auth/profile', data)
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/auth/password', data)
}

export function logout(token: string) {
  return request.post('/auth/logout', { token })
}
```

**关键原则**：
- 每个函数的入参和返回值都有明确的 TypeScript 类型
- 类型名和 API 文档中的字段名一致
- 不写 `any`
- 泛型标注 `request.post<{ data: LoginResult }>` 让调用方自动推导

### 3.2 request.ts 拦截器 — 借鉴李旋

```typescript
// api/request.ts
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'   // shop-web
// import { useAdminStore } from '@/stores/admin' // admin-web
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截：自动带 token
request.interceptors.request.use((config) => {
  const store = useUserStore()  // 或 useAdminStore()
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

// 响应拦截：统一解包 + 401 处理
request.interceptors.response.use(
  (response) => {
    const data = response.data
    // 你们的 API 格式是 { success: true, message: "...", data: {...} }
    if (data.success === false) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data  // 直接返回 { success, data } ，调用方取 .data
  },
  (error) => {
    if (error.response?.status === 401) {
      const store = useUserStore()
      store.logout()
      const path = router.currentRoute.value.path
      if (!['/login', '/register', '/forgot-password'].includes(path)) {
        ElMessage.warning('登录已过期，请重新登录')
        router.push({ path: '/login', query: { redirect: path } })
      }
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
```

### 3.3 Store 层 — 借鉴李旋 + 代宇霆（薄组件原则）

**user store — 登录态持久化**：

```typescript
// stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginResult } from '@/api/user'
import { markFreshLogin, consumeFreshLogin } from '@/utils/freshLogin'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userId = ref<number | null>(null)
  const nickname = ref('')
  const avatarUrl = ref('')
  const email = ref('')
  const phone = ref('')

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data: LoginResult) {
    token.value = data.token
    userId.value = data.userId
    nickname.value = data.nickname || ''
    markFreshLogin()  // 触发 cart/favorite 刷新
  }

  function setProfile(data: { nickname?: string; avatarUrl?: string; email?: string; phone?: string }) {
    if (data.nickname !== undefined) nickname.value = data.nickname
    if (data.avatarUrl !== undefined) avatarUrl.value = data.avatarUrl
    if (data.email !== undefined) email.value = data.email
    if (data.phone !== undefined) phone.value = data.phone
  }

  function logout() {
    token.value = ''
    userId.value = null
    nickname.value = ''
    avatarUrl.value = ''
    email.value = ''
    phone.value = ''
    consumeFreshLogin()
  }

  return { token, userId, nickname, avatarUrl, email, phone, isLoggedIn, setAuth, setProfile, logout }
}, { persist: true })
```

**cart store — 只存 itemCount，不存完整购物车**：

```typescript
// stores/cart.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useUserStore } from './user'
import { fetchCart } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const itemCount = ref(0)

  async function refresh() {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) { itemCount.value = 0; return }
    try {
      const res = await fetchCart()
      itemCount.value = (res.data as any)?.items?.reduce((s: number, i: any) => s + i.quantity, 0) ?? 0
    } catch { itemCount.value = 0 }
  }

  return { itemCount, refresh }
})
```

**favorite store — 乐观更新 + 回滚**：

```typescript
// stores/favorite.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { addFavorite, removeFavorite, fetchFavorites } from '@/api/product'

export const useFavoriteStore = defineStore('favorite', () => {
  const ids = ref<Set<number>>(new Set())
  const loading = ref(false)

  function isFavorite(productId: number) { return ids.value.has(productId) }

  async function refresh() {
    loading.value = true
    try {
      const res = await fetchFavorites()
      const list = Array.isArray(res.data) ? res.data : []
      ids.value = new Set(list.map((item: any) => item.productId ?? item.id))
    } catch { ids.value = new Set() }
    finally { loading.value = false }
  }

  function clear() { ids.value = new Set() }

  async function toggle(productId: number) {
    const wasFavorite = isFavorite(productId)
    // 乐观更新：先改 UI
    if (wasFavorite) ids.value.delete(productId)
    else ids.value.add(productId)
    try {
      if (wasFavorite) await removeFavorite(productId)
      else await addFavorite(productId)
      return !wasFavorite
    } catch {
      // 回滚
      if (wasFavorite) ids.value.add(productId)
      else ids.value.delete(productId)
      throw error
    }
  }

  return { ids, loading, isFavorite, refresh, clear, toggle }
})
```

**admin store — 权限数组**：

```typescript
// stores/admin.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref('')
  const adminId = ref<number | null>(null)
  const role = ref('')          // 'ADMIN' | 'SUPER_ADMIN'
  const permissions = ref<string[]>([])

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data: { token: string; adminId: number; role: string }) {
    token.value = data.token
    adminId.value = data.adminId
    role.value = data.role
    // 如果是 SUPER_ADMIN 则拥有所有权限
    if (data.role === 'SUPER_ADMIN') {
      permissions.value = ['*']
    }
  }

  function hasPermission(code: string): boolean {
    return role.value === 'SUPER_ADMIN' || permissions.value.includes(code)
  }

  function logout() {
    token.value = ''
    adminId.value = null
    role.value = ''
    permissions.value = []
  }

  return { token, adminId, role, permissions, isLoggedIn, setAuth, hasPermission, logout }
}, { persist: true })
```

### 3.4 Router 层 — 借鉴李旋的 meta 驱动

**shop-web 路由**：

```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('@/views/HomeView.vue'), meta: { title: '首页' } },
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { title: '登录', guest: true } },
    { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue'), meta: { title: '注册', guest: true } },
    { path: '/forgot-password', name: 'forgot-password', component: () => import('@/views/ForgotPasswordView.vue'), meta: { title: '找回密码', guest: true } },
    { path: '/category/:id', name: 'category', component: () => import('@/views/CategoryView.vue'), meta: { title: '分类商品' } },
    { path: '/search', name: 'search', component: () => import('@/views/SearchView.vue'), meta: { title: '搜索' } },
    { path: '/product/:id', name: 'product-detail', component: () => import('@/views/ProductDetailView.vue'), meta: { title: '商品详情' } },
    { path: '/cart', name: 'cart', component: () => import('@/views/CartView.vue'), meta: { title: '购物车', auth: true } },
    { path: '/checkout', name: 'checkout', component: () => import('@/views/CheckoutView.vue'), meta: { title: '确认订单', auth: true } },
    { path: '/payment', name: 'payment', component: () => import('@/views/PaymentView.vue'), meta: { title: '订单支付', auth: true } },
    { path: '/orders', name: 'orders', component: () => import('@/views/OrdersView.vue'), meta: { title: '我的订单', auth: true } },
    { path: '/orders/:id', name: 'order-detail', component: () => import('@/views/OrderDetailView.vue'), meta: { title: '订单详情', auth: true } },
    { path: '/profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { title: '个人中心', auth: true } },
    { path: '/address', name: 'address', component: () => import('@/views/AddressView.vue'), meta: { title: '收货地址', auth: true } },
    { path: '/favorites', name: 'favorites', component: () => import('@/views/FavoritesView.vue'), meta: { title: '我的收藏', auth: true } },
    { path: '/coupons', name: 'coupons', component: () => import('@/views/CouponView.vue'), meta: { title: '优惠券', auth: true } },
    { path: '/seckill', name: 'seckill', component: () => import('@/views/SeckillView.vue'), meta: { title: '限时秒杀' } },
    { path: '/notices', name: 'notices', component: () => import('@/views/NoticeListView.vue'), meta: { title: '公告活动' } },
    { path: '/feedback', name: 'feedback', component: () => import('@/views/FeedbackView.vue'), meta: { title: '意见反馈', auth: true } },
    { path: '/service', name: 'service', component: () => import('@/views/CustomerServiceView.vue'), meta: { title: '在线客服', auth: true } },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.auth && !userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guest && userStore.isLoggedIn) {
    return { path: '/' }
  }
})

router.afterEach((to) => {
  document.title = `${to.meta.title || '商城'} - 优品商城`
})

export default router
```

**admin-web 路由**（关键：permission + superAdminOnly）：

```typescript
// router/index.ts
const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { title: '管理员登录', guest: true } },
  { path: '/dashboard', name: 'dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '数据看板', auth: true, permission: 'dashboard:view' } },
  { path: '/users', name: 'users', component: () => import('@/views/UsersView.vue'), meta: { title: '用户管理', auth: true, permission: 'user:manage' } },
  { path: '/categories', name: 'categories', component: () => import('@/views/CategoriesView.vue'), meta: { title: '分类管理', auth: true, permission: 'category:manage' } },
  { path: '/products', name: 'products', component: () => import('@/views/ProductsView.vue'), meta: { title: '商品管理', auth: true, permission: 'product:manage' } },
  { path: '/promotions', name: 'promotions', component: () => import('@/views/PromotionsView.vue'), meta: { title: '促销管理', auth: true, permission: 'promotion:manage' } },
  { path: '/reviews', name: 'reviews', component: () => import('@/views/ReviewsView.vue'), meta: { title: '评价管理', auth: true, permission: 'review:manage' } },
  { path: '/orders', name: 'orders', component: () => import('@/views/OrdersView.vue'), meta: { title: '订单管理', auth: true, permission: 'order:manage' } },
  { path: '/banners', name: 'banners', component: () => import('@/views/BannersView.vue'), meta: { title: '轮播管理', auth: true, permission: 'banner:manage' } },
  { path: '/notices', name: 'notices', component: () => import('@/views/NoticesView.vue'), meta: { title: '公告管理', auth: true, permission: 'notice:manage' } },
  { path: '/feedbacks', name: 'feedbacks', component: () => import('@/views/FeedbacksView.vue'), meta: { title: '反馈管理', auth: true, permission: 'feedback:manage' } },
  { path: '/cs', name: 'cs', component: () => import('@/views/CsView.vue'), meta: { title: '客服咨询', auth: true, permission: 'cs:manage' } },
  { path: '/permission-manage', name: 'permission-manage', component: () => import('@/views/PermissionManageView.vue'), meta: { title: '权限管理', auth: true, superAdminOnly: true } },
  { path: '/profile', name: 'profile', component: () => import('@/views/AdminProfileView.vue'), meta: { title: '个人中心', auth: true } },
]

router.beforeEach((to) => {
  const adminStore = useAdminStore()
  if (to.meta.auth && !adminStore.isLoggedIn) return { path: '/login' }
  if (to.meta.guest && adminStore.isLoggedIn) return { path: '/dashboard' }
  if (to.meta.permission && !adminStore.hasPermission(to.meta.permission as string)) {
    ElMessage.warning('您没有访问该页面的权限')
    return { path: '/dashboard' }
  }
  if (to.meta.superAdminOnly && adminStore.role !== 'SUPER_ADMIN') {
    ElMessage.warning('仅超级管理员可访问')
    return { path: '/dashboard' }
  }
})
```

### 3.5 v-permission 指令 — 李旋独有的按钮级权限

```typescript
// directives/permission.ts
import type { App, Directive } from 'vue'
import { useAdminStore } from '@/stores/admin'

const permissionDirective: Directive<HTMLElement, string> = {
  mounted(el, binding) {
    const adminStore = useAdminStore()
    if (!adminStore.hasPermission(binding.value)) {
      el.style.display = 'none'
    }
  },
}

export function setupPermissionDirective(app: App) {
  app.directive('permission', permissionDirective)
}

// 使用示例：<el-button v-permission="'user:manage'">创建用户</el-button>
```

### 3.6 App.vue 布局切换 — 借鉴李旋

**shop-web App.vue**：

```vue
<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import RightSideBar from '@/components/layout/RightSideBar.vue'
import ActivityNoticeBanner from '@/components/notice/ActivityNoticeBanner.vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useFavoriteStore } from '@/stores/favorite'

const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const favoriteStore = useFavoriteStore()

const isHome = computed(() => route.path === '/')
const isAuthPage = computed(() => route.meta.guest === true)
const showChrome = computed(() => !isAuthPage.value)

onMounted(() => {
  if (userStore.isLoggedIn) {
    cartStore.refresh()
    favoriteStore.refresh()
  }
})

watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) { cartStore.refresh(); favoriteStore.refresh() }
  else { favoriteStore.clear() }
})
</script>

<template>
  <div class="app-layout">
    <ActivityNoticeBanner v-if="showChrome" />
    <AppHeader v-if="!isHome && showChrome" />
    <RightSideBar v-if="showChrome" />
    <main class="app-main" :class="{ 'app-main--home': isHome || route.meta.fullWidth }">
      <router-view />
    </main>
  </div>
</template>
```

**admin-web App.vue**：桌面侧边栏 + 移动端抽屉

```vue
<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Menu } from '@element-plus/icons-vue'
import AdminSidebar from '@/components/layout/AdminSidebar.vue'

const route = useRoute()
const isLoginPage = computed(() => route.path === '/login')
const drawerVisible = ref(false)
</script>

<template>
  <div class="admin-layout" :class="{ 'admin-layout--login': isLoginPage }">
    <template v-if="!isLoginPage">
      <!-- 移动端菜单按钮 -->
      <button class="mobile-menu-btn" @click="drawerVisible = true">
        <el-icon><Menu /></el-icon>
      </button>
      <!-- 桌面端侧边栏 -->
      <AdminSidebar class="desktop-sidebar" />
      <!-- 移动端抽屉 -->
      <el-drawer v-model="drawerVisible" direction="ltr" size="260px" :with-header="false">
        <AdminSidebar mobile @navigate="drawerVisible = false" />
      </el-drawer>
      <main class="admin-main"><router-view /></main>
    </template>
    <router-view v-else />
  </div>
</template>
```

---

## 四、融合十人长处的对照表

| 来源 | 取其什么 | 体现在哪里 |
|------|---------|-----------|
| **李旋** (满分基准) | 双项目独立、v-permission、pinia persist、meta路由守卫、CSS变量主题、乐观更新favorite、移动端抽屉 | 整个架构骨架 |
| **commerce-web** | API-First：每个API文件先定义类型再写函数、返回类型用泛型标注 | `api/*.ts` 全部文件 |
| **张臻** | TypeScript 严格度：`strict: true` + `noUnusedLocals` + `noUnusedParameters` | `tsconfig.app.json` |
| **代宇霆** | 页面组件薄——业务逻辑在store，页面 < 200行（除了ProductDetail这种复杂页面） | views 实现原则 |
| **杨浩伟** | WebSocket 聊天集成——对接你们后端已有的 `ChatWebSocketHandler` | `CustomerServiceView.vue` + `api/cs.ts` |
| **郭振顺** | 中国省市区数据——地址选择用 `element-china-area-data` | `AddressView.vue` |
| **李伟翔** | 多阶段 Docker 构建（你们已有，沿用） | `docker/Dockerfile.shop-web` |
| **e_commerce** | 6 数据库拆分 + RabbitMQ（你们已有，不是前端范围） | — |
| **web-dev-experiment** | 后端测试验证前端——每写完一个 store 就运行对应的后端测试确认 | 开发流程 |

---

## 五、开发顺序（不可逆依赖链）

```
Step 1: 项目骨架
  ├── npm create vite@latest shop-web -- --template vue-ts
  ├── npm create vite@latest admin-web -- --template vue-ts
  ├── 安装依赖（package.json 按上述清单）
  ├── 配置 vite.config.ts（proxy + alias）
  ├── 配置 tsconfig.app.json（strict + paths）
  └── 配置 index.html（中文 title + 字体）

Step 2: 类型 + API 层（先 shop-web 后 admin-web）
  ├── api/request.ts          ← Axios 实例 + 拦截器
  ├── api/user.ts             ← 类型定义 → 函数导出
  ├── api/product.ts
  ├── api/cart.ts
  ├── api/order.ts
  ├── api/address.ts
  ├── api/coupon.ts
  ├── api/promotion.ts
  ├── api/notice.ts（含 home 接口）
  ├── api/cs.ts
  └── api/feedback.ts
  验证：在浏览器 console 调 request.get('/products') 确认能通

Step 3: Store 层
  ├── stores/user.ts          ← Pinia + persist
  ├── stores/cart.ts
  ├── stores/favorite.ts
  └── utils/freshLogin.ts
  验证：console 调 userStore.login() 确认 token 写入 localStorage

Step 4: Router 层
  ├── router/index.ts         ← 路由表 + beforeEach 守卫
  验证：页面间跳转，登录/未登录行为正确

Step 5: 全局样式 + 布局组件
  ├── styles/global.css       ← CSS 变量 + 全局 reset
  ├── App.vue                 ← 布局切换逻辑
  ├── components/layout/AppHeader.vue
  ├── components/layout/HomeSearchBar.vue
  ├── components/layout/RightSideBar.vue
  └── components/auth/AuthLayout.vue
  验证：首页、登录页、商品列表页三种布局正确渲染

Step 6: 页面（按用户路径顺序）
  ├── HomeView.vue            ← 轮播 + 分类 + 热门 + 新品 + 搜索
  ├── CategoryView.vue        ← /category/:id  分类筛选
  ├── SearchView.vue          ← /search?keyword=  搜索结果
  ├── ProductDetailView.vue   ← 最复杂页面（图片+规格+评价+收藏+加购）
  ├── LoginView.vue
  ├── RegisterView.vue
  ├── ForgotPasswordView.vue
  ├── CartView.vue
  ├── CheckoutView.vue
  ├── PaymentView.vue
  ├── OrdersView.vue          ← 6 状态 Tab
  ├── OrderDetailView.vue     ← 物流轨迹
  ├── ProfileView.vue
  ├── AddressView.vue
  ├── FavoritesView.vue
  ├── CouponView.vue
  ├── SeckillView.vue
  ├── NoticeListView.vue
  ├── FeedbackView.vue
  └── CustomerServiceView.vue ← WebSocket 聊天

Step 7: admin-web 页面（按菜单顺序）
  ├── LoginView.vue
  ├── DashboardView.vue       ← ECharts 三个图表
  ├── UsersView.vue
  ├── CategoriesView.vue
  ├── ProductsView.vue
  ├── PromotionsView.vue
  ├── ReviewsView.vue
  ├── OrdersView.vue
  ├── BannersView.vue
  ├── NoticesView.vue
  ├── FeedbacksView.vue
  ├── CsView.vue
  ├── PermissionManageView.vue
  └── AdminProfileView.vue

Step 8: 响应式 + 打磨
  ├── 390px 手机断点
  ├── 768px 平板断点
  ├── 操作反馈（message + 确认弹窗 + loading）
  ├── 空状态处理（el-empty）
  └── 错误状态处理（网络错误提示 + 重试按钮）
```

---

## 六、关键设计决策

| 决策 | 选择 | 来源 | 理由 |
|------|------|------|------|
| 前后台关系 | 两个独立 Vite 项目 | 李旋 | 老师硬性要求"两个业务" |
| CSS 方案 | 单文件 CSS 变量 | 李旋 | 150 行搞定，不引入 Tailwind 复杂度 |
| 状态持久化 | pinia-plugin-persistedstate | 李旋 | 一行配置，token/购物车自动存 localStorage |
| 购物车 Store | 只存 itemCount，不存完整列表 | 李旋 | 购物车数据从后端实时获取，前端只缓存角标数字 |
| 收藏 Store | 乐观更新 + 失败回滚 | 李旋 | 点击立刻反馈，失败才回退 |
| 权限控制 | v-permission 指令 + 路由 meta | 李旋 | 按钮级 + 页面级双重控制 |
| API 类型 | 每个文件先定义 interface 再写函数 | commerce-web | 类型从 API 文档推导，编译时就能发现对接错误 |
| TypeScript | strict: true | 张臻 | 严格模式不让写烂代码 |
| 页面组件 | 薄组件原则 | 代宇霆 | 逻辑在 store，页面只渲染和派发事件 |
| WebSocket | 对接后端 ChatWebSocketHandler | 杨浩伟 + 你们后端 | 已有基础设施，前端只需连接 |

---

## 七、评分对照表（自检清单）

| # | 评分点 | 分值 | shop-web | admin-web |
|---|--------|------|----------|-----------|
| 1 | 基础-商品 | 3 | `ProductDetailView` + `CategoryView` + `SearchView` | — |
| 2 | 基础-购物车 | 3 | `CartView` | — |
| 3 | 基础-订单 | 4 | `CheckoutView` + `PaymentView` + `OrdersView` + `OrderDetailView` | — |
| 4 | 基础-管理员 | 2 | — | `LoginView` + `AdminSidebar` 退出 |
| 5 | 基础-数据 | 2 | — | `DashboardView` (统计卡片+ECharts) |
| 6 | 基础-用户 | 2 | — | `UsersView` |
| 7 | 基础-后台商品 | 2 | — | `ProductsView` + `CategoriesView` + `ReviewsView` |
| 8 | 基础-后台订单 | 2 | — | `OrdersView` (发货/退款/导出) |
| 9 | 进阶-Element Plus | 1 | el-table/form/dialog/pagination/carousel | el-table/form/drawer/upload |
| 10 | 进阶-Pinia | 1 | user + cart + favorite stores | admin store |
| 11 | 进阶-认证鉴权 | 2 | router guard + axios interceptor | router guard + v-permission + permission codes |
| 12 | 进阶-界面设计 | 4 | 统一 CSS 变量 + 操作反馈 + 空状态 | 统一 CSS 变量 + 操作反馈 + 空状态 |
| 13 | 进阶-用户认证 | 1 | `LoginView` + `RegisterView` + `ForgotPasswordView` + `ProfileView` | — |
| 14 | 进阶-首页 | 1 | `HomeView` (轮播+分类+热门+新品+搜索) | — |
| 15 | 进阶-收货地址 | 1 | `AddressView` (CRUD + 默认 + 省市区) | — |
| 16 | 进阶-规格/优惠券/秒杀 | 1 | `ProductDetailView`(规格) + `CouponView` + `SeckillView` | — |
| 17 | 进阶-轮播管理 | 1 | — | `BannersView` (CRUD + 上传 + 链接 + 排序) |
| 18 | 进阶-系统管理 | 1 | — | `NoticesView` + `FeedbacksView` + `AdminProfileView` |
| 19 | 进阶-导入导出 | 2 | — | `ProductsView`(CSV导入+Excel导出) + `OrdersView`(Excel导出) |
| 20 | 进阶-响应式 | 2 | 390px + 768px 两个断点 | 390px 移动端抽屉菜单 |
| 21 | 进阶-图片上传 | 1 | 评价图片上传 | 商品图/轮播图上传 |
| 22 | 进阶-数据分页 | 1 | 商品列表分页 | 商品/订单/用户列表分页 |
