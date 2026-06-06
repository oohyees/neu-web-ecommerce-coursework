import { createRouter, createWebHistory } from 'vue-router'
import AdminLogin from './views/AdminLogin.vue'
import AdminOrders from './views/AdminOrders.vue'
import AdminProducts from './views/AdminProducts.vue'
import AdminDashboard from './views/AdminDashboard.vue'
import AdminAnnouncements from './views/AdminAnnouncements.vue'
import AdminFeedback from './views/AdminFeedback.vue'
import AdminUsers from './views/AdminUsers.vue'
import AdminBanners from './views/AdminBanners.vue'
import AdminCategories from './views/AdminCategories.vue'
import AdminReviews from './views/AdminReviews.vue'
import AdminProfile from './views/AdminProfile.vue'
import AdminPromotions from './views/AdminPromotions.vue'
import AdminAdmins from './views/AdminAdmins.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/admin/dashboard' },
    { path: '/admin', redirect: '/admin/login' },
    { path: '/admin/login', component: AdminLogin },
    { path: '/admin/dashboard', component: AdminDashboard },
    { path: '/admin/orders', component: AdminOrders },
    { path: '/admin/products', component: AdminProducts },
    { path: '/admin/categories', component: AdminCategories },
    { path: '/admin/reviews', component: AdminReviews },
    { path: '/admin/users', component: AdminUsers },
    { path: '/admin/banners', component: AdminBanners },
    { path: '/admin/announcements', component: AdminAnnouncements },
    { path: '/admin/feedback', component: AdminFeedback },
    { path: '/admin/consultations', component: () => import('./views/AdminConsultations.vue') },
    { path: '/admin/activity-notices', component: () => import('./views/AdminActivityNotices.vue') },
    { path: '/admin/promotions', component: AdminPromotions },
    { path: '/admin/admins', component: AdminAdmins },
    { path: '/admin/profile', component: AdminProfile },
    { path: '/:pathMatch(.*)*', redirect: '/admin/dashboard' }
  ]
})
