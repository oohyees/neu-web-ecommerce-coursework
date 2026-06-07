/**
 * 管理后台 Store 测试
 * 覆盖采分点：管理员认证状态管理、权限控制
 */
import { describe, it, expect, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAdminStore } from '@/stores/admin'

describe('useAdminStore - 管理员状态管理', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('initial state is not logged in', () => {
    const store = useAdminStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.token).toBe('')
    expect(store.adminId).toBeNull()
    expect(store.role).toBe('')
    expect(store.permissions).toEqual([])
  })

  it('setAuth sets token, adminId, role', () => {
    const store = useAdminStore()
    store.setAuth({ token: 'admin-token', adminId: 1, role: 'SUPER_ADMIN' })
    expect(store.token).toBe('admin-token')
    expect(store.adminId).toBe(1)
    expect(store.role).toBe('SUPER_ADMIN')
    expect(store.isLoggedIn).toBe(true)
  })

  it('setAuth grants all permissions for SUPER_ADMIN', () => {
    const store = useAdminStore()
    store.setAuth({ token: 'admin-token', adminId: 1, role: 'SUPER_ADMIN' })
    expect(store.permissions).toEqual(['*'])
  })

  it('setAuth does not grant wildcard permissions for non-SUPER_ADMIN', () => {
    const store = useAdminStore()
    store.setAuth({ token: 'admin-token', adminId: 2, role: 'ADMIN' })
    expect(store.permissions).toEqual([])
  })

  it('updateSession updates adminId and role', () => {
    const store = useAdminStore()
    store.setAuth({ token: 'admin-token', adminId: 1, role: 'ADMIN' })
    store.updateSession({ adminId: 2, role: 'SUPER_ADMIN' })
    expect(store.adminId).toBe(2)
    expect(store.role).toBe('SUPER_ADMIN')
    expect(store.permissions).toEqual(['*'])
  })

  it('hasPermission returns true for SUPER_ADMIN', () => {
    const store = useAdminStore()
    store.setAuth({ token: 'admin-token', adminId: 1, role: 'SUPER_ADMIN' })
    expect(store.hasPermission('any:permission')).toBe(true)
    expect(store.hasPermission('product:create')).toBe(true)
  })

  it('hasPermission checks permissions array for non-SUPER_ADMIN', () => {
    const store = useAdminStore()
    store.setAuth({ token: 'admin-token', adminId: 2, role: 'ADMIN' })
    store.permissions = ['product:read', 'order:read']
    expect(store.hasPermission('product:read')).toBe(true)
    expect(store.hasPermission('product:create')).toBe(false)
  })

  it('logout clears all state', () => {
    const store = useAdminStore()
    store.setAuth({ token: 'admin-token', adminId: 1, role: 'SUPER_ADMIN' })
    store.logout()
    expect(store.token).toBe('')
    expect(store.adminId).toBeNull()
    expect(store.role).toBe('')
    expect(store.permissions).toEqual([])
    expect(store.isLoggedIn).toBe(false)
  })

  it('isLoggedIn is computed from token', () => {
    const store = useAdminStore()
    expect(store.isLoggedIn).toBe(false)
    store.setAuth({ token: 'xyz', adminId: 1, role: 'ADMIN' })
    expect(store.isLoggedIn).toBe(true)
    store.logout()
    expect(store.isLoggedIn).toBe(false)
  })
})
