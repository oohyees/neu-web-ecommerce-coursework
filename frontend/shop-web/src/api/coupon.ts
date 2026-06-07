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
  /** 后端原始字段别名 */
  discountAmount?: number
  thresholdAmount?: number
}

/** 将后端字段映射为前端统一字段 */
export function normalizeCoupon(raw: any): Coupon {
  return {
    ...raw,
    value: raw.value ?? raw.discountAmount ?? 0,
    minAmount: raw.minAmount ?? raw.thresholdAmount ?? 0,
  }
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
