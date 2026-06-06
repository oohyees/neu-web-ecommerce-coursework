export interface ApiResponse<T = any> { code: number; message: string; data: T }
export interface LoginParams { account: string; password: string }
export interface RegisterParams { account: string; password: string; nickname: string; email: string; phone?: string; code?: string }
export interface LoginResult { token: string; userId: number; email: string; nickname: string; avatar?: string }
export interface UserInfo { id: number; account: string; nickname: string; email: string; avatar?: string | null; phone?: string | null }
export interface ProductSearchParams { keyword?: string; categoryId?: number; parentCategoryId?: number; sort?: string; page?: number; pageSize?: number; minPrice?: number; maxPrice?: number; searchMode?: string }
export interface Banner { id: number; title: string; imageUrl: string; linkUrl?: string; sortOrder?: number; bgColor?: string }
export interface Category { id: number; name: string; parentId: number | null; sortOrder?: number; iconUrl?: string; children?: Category[] }
export interface Product { id: number; name: string; price: number; originalPrice?: number; imageUrl: string; categoryId: number; categoryName?: string; stock: number; sales: number; status: 'ON_SALE' | 'OFF_SALE' | 'DELETED'; description?: string; promotionPrice?: number | null; createdAt?: string }
export interface CartItem { id: number; productId: number; userId: number; productName: string; productImage: string; price: number; quantity: number; selected?: boolean; stock?: number }
export interface Address { id: number; userId: number; receiverName: string; phone: string; province: string; city: string; district: string; detail: string; isDefault: boolean }
export interface OrderItem { id: number; productId: number; productName: string; productImage: string; price: number; quantity: number }
export interface Order { id: number; orderNo: string; userId: number; totalAmount: number; status: string; paymentMethod?: string; items: OrderItem[]; address: Address; createdAt: string }
export interface PageResult<T> { records: T[]; total: number; page: number; pageSize: number }
export interface Coupon { id: number; name: string; thresholdAmount: number; discountAmount: number }
export interface Promotion { id: number; title: string; description?: string; promotionType: string; imageUrl?: string; startsAt?: string; products?: Product[] }
export interface Announcement { id: number; title: string; content: string; createdAt: string }
export interface ActivityNotice { id: number; title: string; content: string; type: string; createdAt: string }
export interface Review { id: number; productId: number; userId: number; userName: string; rating: number; content: string; images?: string[]; createdAt: string }
export interface Favorite { id: number; productId: number; productName: string; productImage: string; price: number; createdAt: string }
export interface HomeData { banners: Banner[]; hotProducts: Product[]; newProducts: Product[]; promotionProducts: Product[]; hotSearches?: { keyword: string }[]; categories: Category[]; announcements: Announcement[]; activities: Promotion[]; hotKeywords: string[] }
export interface Consultation { id: number; userId: number; userName: string; question: string; answer?: string; createdAt: string; answeredAt?: string }
export interface Feedback { id: number; userId: number; userName: string; type: string; content: string; status: 'PENDING' | 'PROCESSED'; reply?: string; createdAt: string }
