import request from './request'

export function fetchMyConsultations() {
  return request.get('/consultations')
}

export function submitConsultation(data: { subject: string; content: string }) {
  return request.post('/consultations', data)
}
