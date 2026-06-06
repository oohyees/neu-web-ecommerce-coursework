import request from './request'

// 管理员账号管理
export function fetchAdmins(keyword?: string) {
  return request.get('/auth/admin/admins', { params: keyword ? { keyword } : {} })
}

export function createAdmin(data: { username: string; password: string; nickname?: string; email?: string; phone?: string; role: string }) {
  return request.post('/auth/admin/admins', data)
}

export function updateAdmin(data: { id: number; nickname?: string; email?: string; phone?: string; role?: string }) {
  return request.put('/auth/admin/admins', data)
}

export function deleteAdmin(id: number) {
  return request.delete(`/auth/admin/admins/${id}`)
}
