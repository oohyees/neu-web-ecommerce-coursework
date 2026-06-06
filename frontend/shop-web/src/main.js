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
  const authRequired = to.path.startsWith('/user/') || to.path.startsWith('/pay/')
    || ['/cart','/checkout','/feedback','/consultations'].includes(to.path)
  if (authRequired && !session.userId) return '/login'
})

router.afterEach(() => {
  window.scrollTo(0, 0)
})

app.use(pinia).use(router).use(ElementPlus, { locale: zhCn }).mount('#app')
