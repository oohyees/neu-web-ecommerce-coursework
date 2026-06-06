import { get, post, put, del } from './request'
import type { CartItem } from '../types/index'

export function getCart(userId: number) {
  return get<CartItem[]>('/cart', { userId })
}

export function addToCart(userId: number, productId: number, quantity = 1) {
  return post<void>('/cart/items', { userId, productId, quantity })
}

export function updateCartItem(itemId: number, quantity: number) {
  return put<void>(`/cart/items/${itemId}`, { quantity })
}

export function removeCartItem(itemId: number) {
  return del<void>(`/cart/items/${itemId}`)
}

export function selectCartItem(itemId: number, selected: boolean) {
  return put<void>(`/cart/items/${itemId}/select`, { selected })
}

export function selectAllCartItems(userId: number, selected: boolean) {
  return put<void>('/cart/select-all', { userId, selected })
}

export function clearCart(userId: number) {
  return del<void>('/cart/clear', { params: { userId } })
}
