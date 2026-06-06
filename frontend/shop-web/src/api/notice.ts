import request from './request'

export interface Banner {
  id: number
  imageUrl: string
  linkUrl: string
  sortOrder: number
}

export interface HotProduct {
  id: number
  name: string
  price: number
  imageUrl: string
  sales: number
}

export function fetchHomeData() {
  return request.get('/home')
}

export function fetchAnnouncements() {
  return request.get('/announcements')
}

export function fetchActivityNotices() {
  return request.get('/activity-notices')
}

export function trackSearch(keyword: string) {
  return request.post('/home/search/track', { keyword })
}
