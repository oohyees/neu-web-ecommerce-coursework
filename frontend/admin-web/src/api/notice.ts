import request from './request'

// 公告
export function fetchAnnouncements() {
  return request.get('/announcements')
}

export function createAnnouncement(data: { title: string; content: string }) {
  return request.post('/admin/announcements', data)
}

export function updateAnnouncement(data: { id: number; title: string; content: string }) {
  return request.put('/admin/announcements', data)
}

export function deleteAnnouncement(id: number) {
  return request.delete(`/admin/announcements/${id}`)
}

// 活动通知
export function fetchAdminActivityNotices() {
  return request.get('/admin/activity-notices')
}

export function fetchActivityNotices() {
  return request.get('/activity-notices')
}

export function createActivityNotice(data: { title: string; content: string }) {
  return request.post('/admin/activity-notices', data)
}

export function updateActivityNotice(data: { id: number; title: string; content: string }) {
  return request.put('/admin/activity-notices', data)
}

export function deleteActivityNotice(id: number) {
  return request.delete(`/admin/activity-notices/${id}`)
}
