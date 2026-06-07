import request from './request'

export function fetchMyConsultations() {
  return request.get('/consultations')
}

export function submitConsultation(data: { subject: string; content: string }) {
  return request.post('/consultations', data)
}

export function fetchChatHistory() {
  return request.get('/consultations/chat')
}

export function sendChatMessage(content: string) {
  return request.post('/consultations/chat', { content })
}
