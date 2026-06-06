import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAdminStore } from './stores/admin'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/admin/dashboard' },
  { path: '/admin', redirect: '/admin/login' },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('./views/AdminLogin.vue'),
    meta: { title: '管理员登录', guest: true },
  },
  {
    path: '/admin/dashboard',
    name: 'admin-dashboard',
    component: () => import('./views/AdminDashboard.vue'),
    meta: { title: '数据看板', auth: true },
  },
  {
    path: '/admin/orders',
    name: 'admin-orders',
    component: () => import('./views/AdminOrders.vue'),
    meta: { title: '订单管理', auth: true },
  },
  {
    path: '/admin/products',
    name: 'admin-products',
    component: () => import('./views/AdminProducts.vue'),
    meta: { title: '商品管理', auth: true },
  },
  {
    path: '/admin/categories',
    name: 'admin-categories',
    component: () => import('./views/AdminCategories.vue'),
    meta: { title: '分类管理', auth: true },
  },
  {
    path: '/admin/reviews',
    name: 'admin-reviews',
    component: () => import('./views/AdminReviews.vue'),
    meta: { title: '评价管理', auth: true },
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('./views/AdminUsers.vue'),
    meta: { title: '用户管理', auth: true, superAdmin: true },
  },
  {
    path: '/admin/banners',
    name: 'admin-banners',
    component: () => import('./views/AdminBanners.vue'),
    meta: { title: '轮播管理', auth: true },
  },
  {
    path: '/admin/announcements',
    name: 'admin-announcements',
    component: () => import('./views/AdminAnnouncements.vue'),
    meta: { title: '公告管理', auth: true },
  },
  {
    path: '/admin/feedback',
    name: 'admin-feedback',
    component: () => import('./views/AdminFeedback.vue'),
    meta: { title: '反馈管理', auth: true },
  },
  {
    path: '/admin/consultations',
    name: 'admin-consultations',
    component: () => import('./views/AdminConsultations.vue'),
    meta: { title: '客服咨询', auth: true },
  },
  {
    path: '/admin/activity-notices',
    name: 'admin-activity-notices',
    component: () => import('./views/AdminActivityNotices.vue'),
    meta: { title: '活动通知', auth: true },
  },
  {
    path: '/admin/promotions',
    name: 'admin-promotions',
    component: () => import('./views/AdminPromotions.vue'),
    meta: { title: '促销管理', auth: true },
  },
  {
    path: '/admin/admins',
    name: 'admin-admins',
    component: () => import('./views/AdminAdmins.vue'),
    meta: { title: '管理员管理', auth: true, superAdmin: true },
  },
  {
    path: '/admin/profile',
    name: 'admin-profile',
    component: () => import('./views/AdminProfile.vue'),
    meta: { title: '个人中心', auth: true },
  },
  {
    path: '/admin/import-export',
    name: 'admin-import-export',
    component: () => import('./views/ImportExportView.vue'),
    meta: { title: '导入导出', auth: true },
  },
  { path: '/:pathMatch(.*)*', redirect: '/admin/dashboard' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const adminStore = useAdminStore()

  // 需要登录但未登录 → 跳转登录
  if (to.meta.auth && !adminStore.isLoggedIn) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }

  // 已登录访问登录页 → 跳控制台
  if (to.meta.guest && adminStore.isLoggedIn) {
    return { path: '/admin/dashboard' }
  }

  // 超级管理员专属页面
  if (to.meta.superAdmin && adminStore.role !== 'SUPER_ADMIN') {
    ElMessage.warning('仅超级管理员可访问此页面')
    return { path: '/admin/dashboard' }
  }

  return true
})

router.afterEach((to) => {
  document.title = `${to.meta.title || '管理后台'} - 精选商城`
})

export default router
