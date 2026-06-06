import { get, post, put, del } from './request'
import type { Order, PageResult, Address } from '../types/index'

export function createOrder(data: {
  userId: number
  addressId: number
  cartItemIds: number[]
  paymentMethod: string
  couponId?: number
  remark?: string
}) {
  return post<Order>('/orders', data)
}

export function getOrders(params: {
  userId: number
  status?: string
  page?: number
  pageSize?: number
}) {
  return get<PageResult<Order>>('/orders', params as Record<string, any>)
}

export function getOrderById(orderId: number) {
  return get<Order>(`/orders/${orderId}`)
}

export function cancelOrder(orderId: number) {
  return put<void>(`/orders/${orderId}/cancel`)
}

export function confirmReceipt(orderId: number) {
  return put<void>(`/orders/${orderId}/confirm`)
}

export function requestRefund(orderId: number, reason?: string) {
  return post<void>(`/orders/${orderId}/refund`, { reason })
}

export function payOrder(orderId: number, paymentMethod: string) {
  return post<{ payUrl?: string }>(`/orders/${orderId}/pay`, { paymentMethod })
}

export function getAddresses(userId: number) {
  return get<Address[]>('/addresses', { userId })
}

export function createAddress(data: Omit<Address, 'id'>) {
  return post<Address>('/addresses', data)
}

export function updateAddress(id: number, data: Partial<Address>) {
  return put<Address>(`/addresses/${id}`, data)
}

export function deleteAddress(id: number) {
  return del<void>(`/addresses/${id}`)
}
