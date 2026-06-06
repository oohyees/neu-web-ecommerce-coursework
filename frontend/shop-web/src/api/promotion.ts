import request from './request'

export interface Promotion {
  id: number
  productId: number
  productName: string
  type: string
  discountPrice: number
  startAt: string
  endAt: string
  enabled: boolean
}

export function fetchPromotions() {
  return request.get('/marketing/promotions')
}
