import { createRouter, createWebHistory } from 'vue-router'
import UserLogin from './views/UserLogin.vue'
import ProductList from './views/ProductList.vue'
import CartView from './views/CartView.vue'
import AdminLogin from './views/AdminLogin.vue'
import AdminOrders from './views/AdminOrders.vue'
import AdminProducts from './views/AdminProducts.vue'
import RegisterView from './views/RegisterView.vue'
import HomeView from './views/HomeView.vue'
import ProductDetail from './views/ProductDetail.vue'
import AdminDashboard from './views/AdminDashboard.vue'
import FeedbackView from './views/FeedbackView.vue'
import AdminAnnouncements from './views/AdminAnnouncements.vue'
import AdminFeedback from './views/AdminFeedback.vue'
import AdminUsers from './views/AdminUsers.vue'
import AdminBanners from './views/AdminBanners.vue'
import CheckoutView from './views/CheckoutView.vue'
import AdminCategories from './views/AdminCategories.vue'
import AdminReviews from './views/AdminReviews.vue'
import ForgotPassword from './views/ForgotPassword.vue'
import AdminProfile from './views/AdminProfile.vue'
import AdminPromotions from './views/AdminPromotions.vue'
import AdminAdmins from './views/AdminAdmins.vue'
import PayView from './views/PayView.vue'
import UserLayout from './layouts/UserLayout.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/home', redirect: '/' },
    { path: '/login', component: UserLogin },
    { path: '/register', component: RegisterView },
    { path: '/forgot-password', component: ForgotPassword },
    { path: '/products', component: ProductList },
    { path: '/products/:id', component: ProductDetail },
    { path: '/cart', component: CartView },
    { path: '/checkout', component: CheckoutView },
    { path: '/pay/:id', component: PayView },
    { path: '/profile', redirect: '/user/profile' },
    { path: '/addresses', redirect: '/user/addresses' },
    { path: '/favorites', redirect: '/user/favorites' },
    { path: '/orders', redirect: '/user/orders' },
    {
      path: '/user',
      component: UserLayout,
      children: [
        { path: '', redirect: '/user/profile' },
        { path: 'profile', component: () => import('./views/UserProfile.vue') },
        { path: 'orders', component: () => import('./views/UserOrders.vue') },
        { path: 'addresses', component: () => import('./views/UserAddress.vue') },
        { path: 'favorites', component: () => import('./views/UserFavorites.vue') },
        { path: 'coupons', component: () => import('./views/UserCoupons.vue') },
        { path: 'security', component: () => import('./views/UserSecurity.vue') }
      ]
    },
    { path: '/feedback', component: FeedbackView },
    { path: '/consultations', component: () => import('./views/ConsultationView.vue') },
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
    { path: '/admin/profile', component: AdminProfile }
  ]
})
