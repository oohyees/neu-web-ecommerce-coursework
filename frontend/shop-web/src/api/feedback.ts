import request from './request'

export interface Feedback {
  id: number
  userId: number
  type: string
  content: string
  contact: string
  status: string
  reply: string | null
  createdAt: string
}

export function fetchMyFeedback() {
  return request.get('/feedback')
}

export function submitFeedback(data: { type: string; content: string; contact?: string }) {
  return request.post('/feedback', data)
}
