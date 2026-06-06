import request from './request'

export function fetchAllConsultations(params: { page: number; size: number }) {
  return request.get('/admin/consultations', { params })
}

export function replyConsultation(data: { id: number; reply: string }) {
  return request.put('/admin/consultations', data)
}

export function markConsultationProcessed(id: number) {
  return request.put(`/admin/consultations/${id}/processed`)
}
