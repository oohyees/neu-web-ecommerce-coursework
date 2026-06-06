import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from './stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('./views/HomeView.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('./views/UserLogin.vue'),
    meta: { title: '登录', guest: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('./views/RegisterView.vue'),
    meta: { title: '注册', guest: true },
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: () => import('./views/ForgotPassword.vue'),
    meta: { title: '找回密码', guest: true },
  },
  {
    path: '/products',
    name: 'products',
    component: () => import('./views/ProductList.vue'),
    meta: { title: '商品列表' },
  },
  {
    path: '/products/:id',
    name: 'product-detail',
    component: () => import('./views/ProductDetail.vue'),
    meta: { title: '商品详情' },
  },
  {
    path: '/cart',
    name: 'cart',
    component: () => import('./views/CartView.vue'),
    meta: { title: '购物车', auth: true },
  },
  {
    path: '/checkout',
    name: 'checkout',
    component: () => import('./views/CheckoutView.vue'),
    meta: { title: '确认订单', auth: true },
  },
  {
    path: '/pay/:id',
    name: 'pay',
    component: () => import('./views/PayView.vue'),
    meta: { title: '订单支付', auth: true },
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: () => import('./views/FavoritesView.vue'),
    meta: { title: '我的收藏', auth: true },
  },
  {
    path: '/feedback',
    name: 'feedback',
    component: () => import('./views/FeedbackView.vue'),
    meta: { title: '意见反馈', auth: true },
  },
  {
    path: '/seckill',
    name: 'seckill',
    component: () => import('./views/SeckillView.vue'),
    meta: { title: '限时秒杀' },
  },
  {
    path: '/notices',
    name: 'notices',
    component: () => import('./views/NoticeListView.vue'),
    meta: { title: '活动/公告' },
  },
  { path: '/activities', redirect: '/notices' },
  {
    path: '/consultations',
    name: 'consultations',
    component: () => import('./views/ConsultationView.vue'),
    meta: { title: '在线客服', auth: true },
  },
  {
    path: '/orders/:id/detail',
    name: 'order-detail',
    component: () => import('./views/OrderDetailView.vue'),
    meta: { title: '订单详情', auth: true },
  },
  // 用户中心（嵌套路由）
  {
    path: '/user',
    component: () => import('./layouts/UserLayout.vue'),
    children: [
      { path: '', redirect: '/user/profile' },
      {
        path: 'profile',
        name: 'user-profile',
        component: () => import('./views/UserProfile.vue'),
        meta: { title: '个人信息', auth: true },
      },
      {
        path: 'orders',
        name: 'user-orders',
        component: () => import('./views/UserOrders.vue'),
        meta: { title: '我的订单', auth: true },
      },
      {
        path: 'addresses',
        name: 'user-addresses',
        component: () => import('./views/UserAddress.vue'),
        meta: { title: '收货地址', auth: true },
      },
      {
        path: 'favorites',
        name: 'user-favorites',
        component: () => import('./views/UserFavorites.vue'),
        meta: { title: '我的收藏', auth: true },
      },
      {
        path: 'coupons',
        name: 'user-coupons',
        component: () => import('./views/UserCoupons.vue'),
        meta: { title: '优惠券', auth: true },
      },
      {
        path: 'security',
        name: 'user-security',
        component: () => import('./views/UserSecurity.vue'),
        meta: { title: '安全设置', auth: true },
      },
    ],
  },
  // 兼容旧路径重定向
  { path: '/profile', redirect: '/user/profile' },
  { path: '/orders', redirect: '/user/orders' },
  { path: '/addresses', redirect: '/user/addresses' },
  { path: '/home', redirect: '/' },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// 路由守卫：认证拦截
router.beforeEach((to) => {
  const userStore = useUserStore()

  // 需要登录但未登录 → 跳转登录页
  if (to.meta.auth && !userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 已登录访问登录/注册/找回密码 → 跳首页
  if (to.meta.guest && userStore.isLoggedIn) {
    return { path: '/' }
  }

  return true
})

// 动态更新页面标题
router.afterEach((to) => {
  document.title = `${to.meta.title || '商城'} - 精选商城`
})

export default router
