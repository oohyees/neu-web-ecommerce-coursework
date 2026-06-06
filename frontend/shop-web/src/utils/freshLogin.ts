const FRESH_LOGIN_KEY = 'fresh_login'

/** 标记刚刚登录，App.vue 检测到后会触发 cart/favorite 刷新 */
export function markFreshLogin(): void {
  sessionStorage.setItem(FRESH_LOGIN_KEY, '1')
}

/** 消费 freshLogin 标记，返回 true 表示本次是刚登录 */
export function consumeFreshLogin(): boolean {
  const val = sessionStorage.getItem(FRESH_LOGIN_KEY)
  if (val) {
    sessionStorage.removeItem(FRESH_LOGIN_KEY)
    return true
  }
  return false
}
