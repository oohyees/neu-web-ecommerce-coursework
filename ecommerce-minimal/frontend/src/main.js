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
  if(to.path.startsWith('/admin/') && to.path !== '/admin/login' && !session.adminId) return '/admin/login'
  if(to.path.startsWith('/user/') && !session.userId) return '/login'
  if(['/cart','/checkout','/pay','/feedback','/consultations'].includes(to.path) && !session.userId) return '/login'
  if(to.path.startsWith('/pay/') && !session.userId) return '/login'
  if(['/admin/users','/admin/admins','/admin/activity-notices','/admin/announcements'].includes(to.path) && session.role!=='SUPER_ADMIN') return '/admin/dashboard'
})
app.use(pinia).use(router).use(ElementPlus, { locale: zhCn }).mount('#app')
