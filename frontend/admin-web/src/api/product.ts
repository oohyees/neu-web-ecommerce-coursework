import request from './request'

// 商品管理
export function fetchAdminProducts(params: Record<string, unknown>) {
  return request.get('/products/admin/all', { params })
}

export function createProduct(data: Record<string, unknown>) {
  return request.post('/products/admin', data)
}

export function updateProduct(data: Record<string, unknown>) {
  return request.put('/products/admin', data)
}

export function deleteProduct(id: number) {
  return request.delete(`/products/admin/${id}`)
}

export function forceDeleteProduct(id: number) {
  return request.delete(`/products/admin/${id}/force`)
}

export function exportProducts() {
  return request.get('/products/admin/export', { responseType: 'blob' })
}

export function importProducts(file: File) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/products/admin/import', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 分类管理
export function fetchCategories() {
  return request.get('/categories')
}

export function createCategory(data: Record<string, unknown>) {
  return request.post('/admin/categories', data)
}

export function updateCategory(data: Record<string, unknown>) {
  return request.put('/admin/categories', data)
}

export function deleteCategory(id: number) {
  return request.delete(`/admin/categories/${id}`)
}

// 评价管理
export function fetchAllReviews(params: Record<string, unknown>) {
  return request.get('/reviews/admin/all', { params })
}

export function deleteReview(id: number) {
  return request.delete(`/reviews/admin/${id}`)
}
