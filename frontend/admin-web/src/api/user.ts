import request from './request'

export function fetchUsers(params: Record<string, unknown>) {
  return request.get('/auth/admin/users', { params })
}

export function fetchUserDetail(id: number) {
  return request.get(`/auth/admin/users/${id}`)
}

export function setUserEnabled(id: number, enabled: boolean) {
  return request.put(`/auth/admin/users/${id}/enabled`, null, { params: { enabled } })
}

export function createUser(data: Record<string, unknown>) {
  return request.post('/auth/admin/users', data)
}

export function updateUser(id: number, data: Record<string, unknown>) {
  return request.put(`/auth/admin/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/auth/admin/users/${id}`)
}
