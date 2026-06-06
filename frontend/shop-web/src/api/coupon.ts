import request from './request'

export interface Coupon {
  id: number
  name: string
  type: string
  value: number
  minAmount: number
  totalCount: number
  receivedCount: number
  startAt: string
  endAt: string
  enabled: boolean
}

export function fetchAvailableCoupons() {
  return request.get('/marketing/coupons')
}

export function fetchMyCoupons() {
  return request.get('/marketing/coupons/user/0')
}

export function claimCoupon(couponId: number) {
  return request.post(`/marketing/coupons/${couponId}/claim`)
}
