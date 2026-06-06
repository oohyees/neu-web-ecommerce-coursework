import { get, post } from './request'
import type { Feedback, Consultation } from '../types/index'

export function submitFeedback(data: { userId: number; type: string; content: string }) {
  return post<void>('/feedback', data)
}

export function getFeedbacks(userId: number) {
  return get<Feedback[]>('/feedback', { userId })
}

export function submitConsultation(data: { userId: number; question: string }) {
  return post<Consultation>('/consultations', data)
}

export function getConsultations(userId: number) {
  return get<Consultation[]>('/consultations', { userId })
}
