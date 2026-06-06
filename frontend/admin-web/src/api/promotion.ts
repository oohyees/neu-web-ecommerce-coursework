import request from './request'

// 促销管理
export function fetchAdminPromotions(params?: { keyword?: string }) {
  return request.get('/marketing/admin/promotions', { params })
}

export function createPromotion(data: Record<string, unknown>) {
  return request.post('/marketing/admin/promotions', data)
}

export function updatePromotion(data: Record<string, unknown>) {
  return request.put('/marketing/admin/promotions', data)
}

export function deletePromotion(id: number) {
  return request.delete(`/marketing/admin/promotions/${id}`)
}

// 优惠券管理
export function fetchAdminCoupons(params?: { keyword?: string }) {
  return request.get('/marketing/admin/coupons', { params })
}

export function createCoupon(data: Record<string, unknown>) {
  return request.post('/marketing/admin/coupons', data)
}

export function updateCoupon(data: Record<string, unknown>) {
  return request.put('/marketing/admin/coupons', data)
}

export function deleteCoupon(id: number) {
  return request.delete(`/marketing/admin/coupons/${id}`)
}
