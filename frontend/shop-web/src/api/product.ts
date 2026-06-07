import request from './request'

// ═══ 类型 ═══
export interface Product {
  id: number
  categoryId: number
  name: string
  subtitle?: string
  price: number
  originalPrice?: number
  stock: number
  sales: number
  isOnSale: boolean
  imageUrl: string
  detailHtml?: string
  paramsText?: string
  brand?: string
  rating?: number
  discountPercentage?: number
  sku?: string
  images?: string[]
}

export interface ProductSku {
  id: number
  productId: number
  skuCode: string
  color: string | null
  size: string | null
  price: number
  stock: number
  image: string | null
}

export interface ProductImage {
  id: number
  productId: number
  url: string
  sortOrder: number
}

export interface ProductSpec {
  id: number
  productId: number
  specName: string
  specValue: string
}

export interface ProductDetail {
  product: Product
  skus: ProductSku[]
  images: ProductImage[]
  specs: ProductSpec[]
}

export interface Category {
  id: number
  name: string
  parentId: number | null
  sortOrder: number
  children?: Category[]
}

export interface Review {
  id: number
  productId: number
  userId: number
  nickname: string
  avatarUrl: string
  rating: number
  content: string
  imageUrl: string | null
  createdAt: string
}

// ═══ API ═══
export function fetchProducts(params: {
  categoryId?: number
  keyword?: string
  searchMode?: 'exact' | 'fuzzy'
  sort?: string
  page?: number
  size?: number
}) {
  return request.get('/products', params)
}

function parseSpecs(product: Product): ProductSpec[] {
  if (!product.paramsText) return []
  return product.paramsText
    .split(';')
    .map((part, index) => {
      const [name, ...valueParts] = part.split(':')
      return {
        id: index + 1,
        productId: product.id,
        specName: name?.trim() || `参数${index + 1}`,
        specValue: valueParts.join(':').trim(),
      }
    })
    .filter((spec) => spec.specName && spec.specValue)
}

export async function fetchProductDetail(id: number): Promise<ProductDetail> {
  const detail: any = await request.get<ProductDetail | Product>(`/products/${id}`)
  if (detail.product) return detail

  return {
    product: detail as Product,
    skus: [],
    images: detail.imageUrl ? [{ id: 1, productId: detail.id, url: detail.imageUrl, sortOrder: 0 }] : [],
    specs: parseSpecs(detail as Product),
  }
}

export function fetchCategories() {
  return request.get('/categories')
}

export function fetchReviews(productId: number, page = 1, size = 10) {
  return request.get('/reviews', { productId, page, size })
}

export function submitReview(data: { productId: number; rating: number; content: string; imageUrl?: string }) {
  return request.post('/reviews', data)
}

export function fetchFavorites() {
  return request.get('/favorites')
}

export function addFavorite(productId: number) {
  return request.post(`/favorites/${productId}`)
}

export function removeFavorite(productId: number) {
  return request.delete(`/favorites/${productId}`)
}

export function checkFavoriteStatus(productId: number) {
  return request.get(`/favorites/${productId}/status`)
}
