import request from './request'

export function fetchAdminOrders(params: Record<string, unknown>) {
  return request.get('/admin/orders', { params })
}

export function fetchAdminOrderDetail(id: number) {
  return request.get(`/admin/orders/${id}`)
}

export function shipOrder(id: number) {
  return request.put(`/admin/orders/${id}/ship`)
}

export function approveRefund(id: number) {
  return request.put(`/admin/orders/${id}/refund/approve`)
}

export function updateOrderStatus(id: number, status: string) {
  return request.put(`/admin/orders/${id}/status`, null, { params: { status } })
}

export function exportOrders() {
  return request.get('/admin/orders/export', { responseType: 'blob' })
}
