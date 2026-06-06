import request from './request'

export interface CartItem {
  id: number
  productId: number
  productName: string
  skuId: number | null
  skuCode: string | null
  color: string | null
  size: string | null
  specText: string | null
  imageUrl: string
  price: number
  quantity: number
  stock: number
}

export function fetchCart() {
  return request.get('/cart')
}

export function addToCart(data: { productId: number; skuId?: number; specText?: string; quantity: number }) {
  return request.post('/cart/items', data)
}

export function updateCartQuantity(data: { cartItemId?: number; productId?: number; specText?: string; quantity: number }) {
  return request.put('/cart/items', data)
}

export function removeCartItem(params: { cartItemId?: number; productId?: number; specText?: string }) {
  return request.delete('/cart/items', { params })
}
