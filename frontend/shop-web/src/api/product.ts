import { get, post } from './request'
import type { Product, ProductSearchParams, Category, HomeData, Review, PageResult } from '../types/index'

export function fetchHome() {
  return get<HomeData>('/home')
}

export function getProducts(params: ProductSearchParams) {
  return get<PageResult<Product>>('/products', params as Record<string, any>)
}

export function getProductById(id: number) {
  return get<Product>(`/products/${id}`)
}

export function getCategories() {
  return get<Category[]>('/categories')
}

export function getProductReviews(productId: number, page?: number) {
  return get<PageResult<Review>>(`/products/${productId}/reviews`, { page })
}

export function submitReview(productId: number, data: { rating: number; content: string; images?: string[] }) {
  return post<void>(`/products/${productId}/reviews`, data)
}

export function trackSearch(keyword: string) {
  return post<void>('/home/search/track', { keyword: keyword.trim() })
}
