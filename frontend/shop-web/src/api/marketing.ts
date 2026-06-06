import { get, post } from './request'
import type { Coupon, Promotion, ActivityNotice, Announcement } from '../types/index'

export function getCoupons() {
  return get<Coupon[]>('/marketing/coupons')
}

export function claimCoupon(couponId: number, userId: number) {
  return post<void>(`/marketing/coupons/${couponId}/claim`, null, { userId })
}

export function getUserCoupons(userId: number) {
  return get<Coupon[]>('/marketing/coupons/my', { userId })
}

export function getPromotions() {
  return get<Promotion[]>('/marketing/promotions')
}

export function getActivityNotices() {
  return get<ActivityNotice[]>('/activity-notices')
}

export function getAnnouncements() {
  return get<Announcement[]>('/announcements')
}
