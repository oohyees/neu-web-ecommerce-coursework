import request from './request'

export interface Promotion {
  id: number
  productId: number
  title: string
  promotionType: string
  promotionPrice: number
  promotionStock?: number | null
  productName?: string
  imageUrl?: string
  originalPrice?: number
  startAt: string
  endAt: string
  enabled: boolean
}

export function fetchPromotions() {
  return request.get('/marketing/promotions')
}
