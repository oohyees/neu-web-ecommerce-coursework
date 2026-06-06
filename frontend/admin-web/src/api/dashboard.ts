import request from './request'

export function fetchDashboard() {
  return request.get('/admin/dashboard')
}

export function exportDashboard() {
  return request.get('/admin/dashboard/export', { responseType: 'blob' })
}
