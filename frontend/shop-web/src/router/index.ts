import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { title: '首页' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录', guest: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { title: '注册', guest: true },
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('@/views/ForgotPasswordView.vue'),
      meta: { title: '找回密码', guest: true },
    },
    {
      path: '/category/:id',
      name: 'category',
      component: () => import('@/views/CategoryView.vue'),
      meta: { title: '分类商品' },
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/SearchView.vue'),
      meta: { title: '搜索' },
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('@/views/SearchView.vue'),
      meta: { title: '商品列表' },
    },
    {
      path: '/product/:id',
      name: 'product-detail',
      component: () => import('@/views/ProductDetailView.vue'),
      meta: { title: '商品详情' },
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/views/CartView.vue'),
      meta: { title: '购物车', auth: true },
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('@/views/CheckoutView.vue'),
      meta: { title: '确认订单', auth: true },
    },
    {
      path: '/payment',
      name: 'payment',
      component: () => import('@/views/PaymentView.vue'),
      meta: { title: '订单支付', auth: true },
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/OrdersView.vue'),
      meta: { title: '我的订单', auth: true },
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('@/views/OrderDetailView.vue'),
      meta: { title: '订单详情', auth: true },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { title: '个人中心', auth: true },
    },
    {
      path: '/address',
      name: 'address',
      component: () => import('@/views/AddressView.vue'),
      meta: { title: '收货地址', auth: true },
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/FavoritesView.vue'),
      meta: { title: '我的收藏', auth: true },
    },
    {
      path: '/coupons',
      name: 'coupons',
      component: () => import('@/views/CouponView.vue'),
      meta: { title: '优惠券', auth: true },
    },
    {
      path: '/seckill',
      name: 'seckill',
      component: () => import('@/views/SeckillView.vue'),
      meta: { title: '限时秒杀' },
    },
    {
      path: '/notices',
      name: 'notices',
      component: () => import('@/views/NoticeListView.vue'),
      meta: { title: '公告活动' },
    },
    {
      path: '/activities',
      redirect: '/notices',
    },
    {
      path: '/feedback',
      name: 'feedback',
      component: () => import('@/views/FeedbackView.vue'),
      meta: { title: '意见反馈', auth: true },
    },
    {
      path: '/service',
      name: 'service',
      component: () => import('@/views/CustomerServiceView.vue'),
      meta: { title: '在线客服', auth: true },
    },
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
