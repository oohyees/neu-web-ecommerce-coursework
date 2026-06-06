import { get, post, del } from './request'
import type { Favorite } from '../types/index'

export function getFavorites(userId: number) {
  return get<Favorite[]>('/favorites', { userId })
}

export function addFavorite(productId: number, userId: number) {
  return post<void>(`/favorites/${productId}`, null, { userId })
}

export function removeFavorite(productId: number, userId: number) {
  return del<void>(`/favorites/${productId}`, { params: { userId } })
}
