import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import './styles.css'
import App from './App.vue'
import router from './router'
import { useSessionStore } from './store'

const app=createApp(App), pinia=createPinia()
router.beforeEach((to)=>{
  const session=useSessionStore(pinia)
  // 需要登录的路径
  const authRequired = to.path.startsWith('/admin/') || to.path.startsWith('/user/') || to.path.startsWith('/pay/')
    || ['/cart','/checkout','/feedback','/consultations'].includes(to.path)
  if (authRequired && to.path !== '/admin/login' && !session.userId && !session.adminId) {
    return session.adminId ? '/admin/login' : '/login'
  }
  if (to.path.startsWith('/admin/') && to.path !== '/admin/login' && !session.adminId) return '/admin/login'
  if (['/admin/users','/admin/admins','/admin/activity-notices','/admin/announcements'].includes(to.path) && session.role!=='SUPER_ADMIN') return '/admin/dashboard'
})

router.afterEach(() => {
  window.scrollTo(0, 0)
})

app.use(pinia).use(router).use(ElementPlus, { locale: zhCn }).mount('#app')
