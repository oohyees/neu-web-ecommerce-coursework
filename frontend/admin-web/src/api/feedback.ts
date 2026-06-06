import request from './request'

export function fetchAllFeedback(params: { page: number; size: number }) {
  return request.get('/admin/feedback', { params })
}

export function replyFeedback(data: { id: number; reply: string }) {
  return request.put('/admin/feedback', data)
}

export function markFeedbackProcessed(id: number) {
  return request.put(`/admin/feedback/${id}/processed`)
}
