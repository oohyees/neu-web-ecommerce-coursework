import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '管理员登录', guest: true },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { title: '数据看板', auth: true, permission: 'dashboard:view' },
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('@/views/UsersView.vue'),
      meta: { title: '用户管理', auth: true, permission: 'user:manage' },
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('@/views/CategoriesView.vue'),
      meta: { title: '分类管理', auth: true, permission: 'category:manage' },
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('@/views/ProductsView.vue'),
      meta: { title: '商品管理', auth: true, permission: 'product:manage' },
    },
    {
      path: '/promotions',
      name: 'promotions',
      component: () => import('@/views/PromotionsView.vue'),
      meta: { title: '促销管理', auth: true, permission: 'promotion:manage' },
    },
    {
      path: '/reviews',
      name: 'reviews',
      component: () => import('@/views/ReviewsView.vue'),
      meta: { title: '评价管理', auth: true, permission: 'review:manage' },
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/OrdersView.vue'),
      meta: { title: '订单管理', auth: true, permission: 'order:manage' },
    },
    {
      path: '/banners',
      name: 'banners',
      component: () => import('@/views/BannersView.vue'),
      meta: { title: '轮播管理', auth: true, permission: 'banner:manage' },
    },
    {
      path: '/notices',
      name: 'notices',
      component: () => import('@/views/NoticesView.vue'),
      meta: { title: '公告管理', auth: true, permission: 'notice:manage' },
    },
    {
      path: '/feedbacks',
      name: 'feedbacks',
      component: () => import('@/views/FeedbacksView.vue'),
      meta: { title: '反馈管理', auth: true, permission: 'feedback:manage' },
    },
    {
      path: '/cs',
      name: 'cs',
      component: () => import('@/views/CsView.vue'),
      meta: { title: '客服咨询', auth: true, permission: 'cs:manage' },
    },
    {
      path: '/permission-manage',
      name: 'permission-manage',
      component: () => import('@/views/PermissionManageView.vue'),
      meta: { title: '权限管理', auth: true, superAdminOnly: true },
    },
    { path: '/roles', redirect: '/permission-manage' },
    { path: '/admins', redirect: '/permission-manage' },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/AdminProfileView.vue'),
      meta: { title: '个人中心', auth: true },
    },
  ],
})

router.beforeEach((to) => {
  const adminStore = useAdminStore()
  if (to.meta.auth && !adminStore.isLoggedIn) {
    return { path: '/login' }
  }
  if (to.meta.guest && adminStore.isLoggedIn) {
    return { path: '/dashboard' }
  }
  const perm = to.meta.permission as string | undefined
  if (perm && adminStore.isLoggedIn && !adminStore.hasPermission(perm)) {
    ElMessage.warning('您没有访问该页面的权限')
    return { path: '/dashboard' }
  }
  if (to.meta.superAdminOnly && adminStore.isLoggedIn && adminStore.role !== 'SUPER_ADMIN') {
    ElMessage.warning('仅超级管理员可访问')
    return { path: '/dashboard' }
  }
})

router.afterEach((to) => {
  document.title = `${to.meta.title || '管理后台'} - 优品商城`
})

export default router
