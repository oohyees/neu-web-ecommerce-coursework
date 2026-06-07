import { dummyProducts, dummyFlatCategories, dummyReviews, dummyPromotions, dummyBanners } from './dummy-products'

const products = [...dummyProducts]
const categories = dummyFlatCategories

const users = [
  { id: 1, username: 'alice', nickname: 'Alice', email: 'alice@example.com', phone: '13800138000', enabled: true },
  { id: 2, username: 'bob', nickname: 'Bob', email: 'bob@example.com', phone: '13900139000', enabled: true },
  { id: 3, username: 'qa_user', nickname: 'QA 用户', email: 'qa@example.com', phone: '', enabled: false },
]

const orders = [
  { id: 12, orderNo: 'ORD20260607015554700', userId: 1, totalAmount: 153.97, status: 'PENDING', paymentStatus: 'UNPAID', refundStatus: null },
  { id: 11, orderNo: 'ORD20260606183021001', userId: 1, totalAmount: 49.9, status: 'PAID', paymentStatus: 'PAID', refundStatus: null },
  { id: 10, orderNo: 'ORD20260605112233008', userId: 2, totalAmount: 29.99, status: 'COMPLETED', paymentStatus: 'PAID', refundStatus: null },
]

const feedback = [
  { id: 1, userId: 1, type: '体验建议', content: '希望商品列表展示更丰富。', reply: '已优化页面结构。', status: 'REPLIED' },
  { id: 2, userId: 2, type: '页面问题', content: '移动端按钮希望更明显。', reply: null, status: 'PENDING' },
]

const consultations = [
  { id: 1, userId: 1, subject: '发货时间', content: '今天下单什么时候发货？', reply: '24 小时内发货。', status: 'REPLIED' },
  { id: 2, userId: 2, subject: '优惠券使用', content: '优惠券可以叠加吗？', reply: null, status: 'PENDING' },
]

const reviews = [...dummyReviews]

const banners = dummyBanners

const promotions = dummyPromotions

const coupons = [
  { id: 1, name: '新人满 100 减 20', thresholdAmount: 100, discountAmount: 20, enabled: true },
  { id: 2, name: '办公满 200 减 50', thresholdAmount: 200, discountAmount: 50, enabled: true },
]

const announcements = [
  { id: 1, title: '课堂演示版本上线', content: '当前后台页面可静态预览所有内容。' },
  { id: 2, title: '本地图片资源恢复', content: '商品图使用 /catalog 本地资源。' },
]

const activityNotices = [
  { id: 1, title: '六月促销活动', content: '办公商品和家居商品参与活动。' },
  { id: 2, title: '新人优惠券开放', content: '注册用户可以领取优惠券。' },
]

const admins = [
  { id: 1, username: 'admin', nickname: '超级管理员', email: 'admin@example.com', phone: '13800000000', role: 'SUPER_ADMIN' },
  { id: 2, username: 'operator', nickname: '运营管理员', email: 'ops@example.com', phone: '13900000000', role: 'ADMIN' },
]

function page<T>(items: T[]) {
  return { items, total: items.length, page: 1, size: items.length }
}

export function resolveMock(url: string, method: string, data?: any): any {
  const cleanUrl = url.split('?')[0]
  const m = method.toLowerCase()
  if (cleanUrl === '/auth/admin/login') return { token: 'static-admin-token', adminId: 1, role: 'SUPER_ADMIN' }
  if (cleanUrl === '/auth/admin/profile') return { username: 'admin', nickname: '超级管理员', email: 'admin@example.com', phone: '13800000000', role: 'SUPER_ADMIN' }
  if (cleanUrl === '/auth/admin/admins') return admins
  if (cleanUrl === '/auth/admin/users') return page(users)
  if (cleanUrl === '/admin/dashboard') return { userCount: 128, orderCount: 76, salesAmount: 25880, todayOrderCount: 8, todaySalesAmount: 1690, productCount: products.length, salesTrend: [{ day: '06-03', amount: 1200 }, { day: '06-04', amount: 1880 }, { day: '06-05', amount: 2320 }, { day: '06-06', amount: 1690 }], hotProducts: products.slice(0, 5), orderStatus: [{ status: '待支付', value: 8 }, { status: '待发货', value: 15 }, { status: '已完成', value: 53 }] }
  if (cleanUrl === '/products/admin/all') return page(products)
  if (cleanUrl === '/categories') return categories
  if (cleanUrl === '/reviews/admin/all') return page(reviews)
  if (cleanUrl === '/admin/orders') return page(orders)
  if (cleanUrl === '/home/banners') return banners
  if (cleanUrl === '/marketing/admin/promotions') return promotions
  if (cleanUrl === '/marketing/admin/coupons') return coupons
  if (cleanUrl === '/announcements') return announcements
  if (cleanUrl === '/admin/activity-notices') return activityNotices
  if (cleanUrl === '/admin/feedback') return page(feedback)
  if (cleanUrl === '/admin/consultations') return page(consultations)
  if (m === 'post' || m === 'put' || m === 'delete') return data ?? { ok: true }
  return {}
}
