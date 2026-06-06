import request from './request'

export function fetchBanners(keyword?: string) {
  return request.get('/home/banners', { params: keyword ? { keyword } : {} })
}

export function createBanner(data: { imageUrl: string; linkUrl: string; sortOrder: number }) {
  return request.post('/home/banners', data)
}

export function updateBanner(data: { id: number; imageUrl?: string; linkUrl?: string; sortOrder?: number }) {
  return request.put('/home/banners', data)
}

export function deleteBanner(id: number) {
  return request.delete(`/home/banners/${id}`)
}
