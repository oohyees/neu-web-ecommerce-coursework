import type { App, Directive } from 'vue'
import { useAdminStore } from '@/stores/admin'

const permissionDirective: Directive<HTMLElement, string> = {
  mounted(el, binding) {
    const adminStore = useAdminStore()
    if (!adminStore.hasPermission(binding.value)) {
      el.style.display = 'none'
    }
  },
}

export function setupPermissionDirective(app: App) {
  app.directive('permission', permissionDirective)
}
