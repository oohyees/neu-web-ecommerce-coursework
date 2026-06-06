import request from './request'

export interface UserAddress {
  id: number
  userId: number
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: boolean
}

export function fetchAddresses() {
  return request.get('/addresses')
}

export function createAddress(data: Omit<UserAddress, 'id' | 'userId'>) {
  return request.post('/addresses', data)
}

export function updateAddress(data: UserAddress) {
  return request.put('/addresses', data)
}

export function deleteAddress(id: number) {
  return request.delete(`/addresses/${id}`)
}

export function setDefaultAddress(id: number) {
  return request.put(`/addresses/${id}/default`)
}
