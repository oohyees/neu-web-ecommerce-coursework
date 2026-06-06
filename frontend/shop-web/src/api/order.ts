import request from './request'

export interface Order {
  id: number
  orderNo: string
  userId: number
  addressId: number
  couponId: number | null
  totalAmount: number
  status: string
  paymentStatus: string
  paymentMethod: string
  logisticsStatus: string
  refundStatus: string | null
  createdAt: string
}

export interface LogisticsRecord {
  id: number
  orderId: number
  content: string
  createdAt: string
}

export function createOrder(data: {
  addressId: number
  cartItemIds?: number[]
  productIds?: number[]
  couponId?: number
  paymentMethod?: string
}) {
  return request.post('/orders', data)
}

export function fetchMyOrders(params?: { status?: string; page?: number; size?: number }) {
  return request.get('/orders', { params })
}

export function fetchOrderDetail(id: number) {
  return request.get(`/orders/${id}`)
}

export function payOrder(id: number) {
  return request.put(`/orders/${id}/pay`)
}

export function cancelOrder(id: number) {
  return request.put(`/orders/${id}/cancel`)
}

export function confirmOrder(id: number) {
  return request.put(`/orders/${id}/confirm`)
}

export function refundOrder(id: number) {
  return request.put(`/orders/${id}/refund`)
}

export function fetchLogistics(id: number) {
  return request.get(`/orders/${id}/logistics`)
}
