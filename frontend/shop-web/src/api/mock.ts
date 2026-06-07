import { dummyProducts, dummyCategories, dummyReviews, dummyPromotions, dummyBanners } from './dummy-products'

const products = [...dummyProducts]
const categories = dummyCategories

const addresses = [
  { id: 1, userId: 1, receiver: 'Alice', phone: '13800138000', province: '辽宁省', city: '沈阳市', district: '和平区', detail: '文化路 3 号 2-101', isDefault: true },
  { id: 2, userId: 1, receiver: '王同学', phone: '13900001111', province: '辽宁省', city: '沈阳市', district: '浑南区', detail: '创新路 88 号宿舍楼', isDefault: false },
]

const cartItems = products.slice(0, 3).map((p, index) => ({
  id: index + 1,
  productId: p.id,
  productName: p.name,
  skuId: index + 1,
  skuCode: `SKU-${p.id}`,
  color: index === 0 ? '黑色' : '标准',
  size: index === 1 ? '暖光' : '常规',
  specText: index === 0 ? '黑色 / 标准' : '标准',
  imageUrl: p.imageUrl,
  price: p.price,
  quantity: index + 1,
  stock: p.stock,
}))

const coupons = [
  { id: 1, name: '新人满 100 减 20', type: 'FULL_REDUCTION', value: 20, discountAmount: 20, minAmount: 100, thresholdAmount: 100, totalCount: 200, receivedCount: 76, startAt: '2026-06-01', endAt: '2026-06-30', enabled: true, status: 'UNUSED' },
  { id: 2, name: '办公用品满 200 减 50', type: 'FULL_REDUCTION', value: 50, discountAmount: 50, minAmount: 200, thresholdAmount: 200, totalCount: 100, receivedCount: 39, startAt: '2026-06-01', endAt: '2026-06-30', enabled: true, status: 'UNUSED' },
]

const orders = [
  { id: 12, orderNo: 'ORD20260607015554700', userId: 1, addressId: 1, couponId: 1, totalAmount: 153.97, status: 'PENDING', paymentStatus: 'UNPAID', paymentMethod: '模拟支付', logisticsStatus: 'CREATED', refundStatus: null, createdAt: '2026-06-07 01:55:54', items: cartItems.slice(0, 2) },
  { id: 11, orderNo: 'ORD20260606183021001', userId: 1, addressId: 1, couponId: null, totalAmount: 49.90, status: 'SHIPPED', paymentStatus: 'PAID', paymentMethod: '模拟支付', logisticsStatus: 'SHIPPED', refundStatus: null, createdAt: '2026-06-06 18:30:21', items: cartItems.slice(2) },
  { id: 10, orderNo: 'ORD20260605112233008', userId: 1, addressId: 2, couponId: null, totalAmount: 29.99, status: 'COMPLETED', paymentStatus: 'PAID', paymentMethod: '模拟支付', logisticsStatus: 'COMPLETED', refundStatus: null, createdAt: '2026-06-05 11:22:33', items: cartItems.slice(1, 2) },
]

const reviews = [...dummyReviews]

const feedback = [
  { id: 1, userId: 1, type: '体验建议', content: '希望商品列表能显示更多筛选项。', contact: 'alice@example.com', status: 'REPLIED', reply: '已记录，后台会继续优化。', createdAt: '2026-06-07 10:00:00' },
  { id: 2, userId: 1, type: '页面问题', content: '移动端购物车按钮希望更明显。', contact: '13800138000', status: 'PENDING', reply: null, createdAt: '2026-06-07 10:20:00' },
]

const consultations = [
  { id: 1, userId: 1, subject: '发货时间', content: '今天下单什么时候发货？', status: 'REPLIED', reply: '工作日 24 小时内发货。', createdAt: '2026-06-07 09:10:00' },
  { id: 2, userId: 1, subject: '优惠券使用', content: '优惠券可以叠加吗？', status: 'PENDING', reply: null, createdAt: '2026-06-07 09:40:00' },
]

const announcements = [
  { id: 1, title: '优品商城课堂演示版本上线', content: '当前版本支持商品、购物车、订单、后台和微服务演示。' },
  { id: 2, title: '本地图片资源已恢复', content: '商品图片使用 /catalog 本地路径，离线演示不依赖外网。' },
]

const activityNotices = [
  { id: 1, title: '六月办公用品促销', content: '办公设备、数码配件参与满减活动。' },
  { id: 2, title: '新人优惠券开放领取', content: '注册用户可领取新人满减券。' },
]

const promotions = [...dummyPromotions]

function page<T>(items: T[]) {
  return { items, total: items.length, page: 1, size: items.length }
}

export function resolveMock(url: string, method: string, data?: any): any {
  const cleanUrl = url.split('?')[0]
  const m = method.toLowerCase()
  if (cleanUrl === '/auth/login') return { token: 'static-user-token', userId: 1, nickname: 'Alice' }
  if (cleanUrl === '/auth/profile') return { id: 1, username: 'alice', nickname: 'Alice', email: 'alice@example.com', phone: '13800138000', avatarUrl: '/catalog/placeholder.svg' }
  if (cleanUrl === '/home') return { banners: dummyBanners, hotProducts: products.slice(0, 6), newProducts: products.slice(6, 12), hotSearches: ['DummyJSON', '美妆', '香水', '家具', '厨房用品'] }
  if (cleanUrl === '/categories') return categories
  if (cleanUrl === '/products') return page(products)
  if (/^\/products\/\d+$/.test(cleanUrl)) {
    const id = Number(cleanUrl.split('/').pop())
    const product = products.find((p) => p.id === id) || products[0]
    return { product, skus: [
      { id: product.id * 10 + 1, productId: product.id, skuCode: `${product.sku}-V01`, color: null, size: 'Standard', price: product.price, stock: product.stock, image: product.imageUrl },
      { id: product.id * 10 + 2, productId: product.id, skuCode: `${product.sku}-V02`, color: 'Limited', size: 'Gift Set', price: Number((product.price * 1.08).toFixed(2)), stock: Math.max(1, Math.floor(product.stock / 3)), image: product.imageUrl },
    ], images: product.images.map((url: string, index: number) => ({ id: index + 1, productId: product.id, url, sortOrder: index + 1 })), specs: product.paramsText?.split(';').map((part: string, index: number) => { const [specName, specValue] = part.split(':'); return { id: index + 1, productId: product.id, specName, specValue } }) || [] }
  }
  if (cleanUrl === '/reviews') return page(reviews)
  if (cleanUrl === '/favorites') return products.slice(0, 4)
  if (/^\/favorites\/\d+\/status$/.test(cleanUrl)) return true
  if (cleanUrl === '/cart') return { items: cartItems, total: cartItems.length }
  if (cleanUrl === '/addresses') return addresses
  if (cleanUrl === '/marketing/coupons' || cleanUrl === '/marketing/coupons/user/0') return coupons
  if (cleanUrl === '/marketing/promotions') return promotions
  if (cleanUrl === '/orders') return page(orders)
  if (/^\/orders\/\d+$/.test(cleanUrl)) return { order: orders[0], address: addresses[0], items: cartItems, logistics: [{ id: 1, orderId: orders[0].id, content: '订单已创建', createdAt: '2026-06-07 01:55:54' }, { id: 2, orderId: orders[0].id, content: '等待用户支付', createdAt: '2026-06-07 01:56:10' }] }
  if (/^\/orders\/\d+\/logistics$/.test(cleanUrl)) return [{ id: 1, content: '订单已创建', createdAt: '2026-06-07 01:55:54' }]
  if (cleanUrl === '/feedback') return feedback
  if (cleanUrl === '/consultations') return consultations
  if (cleanUrl === '/announcements') return announcements
  if (cleanUrl === '/activity-notices') return activityNotices
  if (m === 'post' && cleanUrl === '/orders') return { orderNo: 'ORD-PREVIEW-20260607' }
  if (m === 'post' || m === 'put' || m === 'delete') return data ?? { ok: true }
  return {}
}
