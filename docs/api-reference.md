# API 参考

> 相关文档：[架构总览](architecture.md) · [开发手册](development.md) · [部署说明](deployment.md)

## 通用约定

- Base URL: `/api`
- 认证：`Authorization: Bearer <token>`（登录/注册等公开端点除外）
- 响应格式：`{ "success": true, "message": "...", "data": {...} }`
- 分页参数：`?page=1&size=10`

> 认证流程详见 [架构文档 § 认证流程](architecture.md)，默认账户见 [开发手册 § 默认账户](development.md)。

## 鉴权说明

| 标记 | 含义 |
|------|------|
| 公开 | 无需 token |
| 登录 | 需要有效 token |
| ADMIN | 需要 ADMIN 或 SUPER_ADMIN 角色 |
| SUPER_ADMIN | 仅 SUPER_ADMIN |

---

## 认证 `/api/auth`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 公开 | 用户登录，返回 `{ token, userId, nickname }` |
| POST | `/api/auth/register` | 公开 | 用户注册 |
| POST | `/api/auth/code` | 公开 | 发送邮箱验证码（1min 节流，10min 有效，真实 SMTP 发信） |
| POST | `/api/auth/register/email` | 公开 | 邮箱验证码注册 |
| POST | `/api/auth/password/reset` | 公开 | 重置密码（需验证码） |
| GET | `/api/auth/profile` | 登录 | 获取当前用户信息 |
| PUT | `/api/auth/profile` | 登录 | 更新个人资料 |
| PUT | `/api/auth/password` | 登录 | 修改当前用户密码 |
| POST | `/api/auth/logout` | 登录 | 登出，销毁 Redis session |
| POST | `/api/auth/admin/login` | 公开 | 管理员登录 |
| GET | `/api/auth/admin/profile` | ADMIN | 管理员资料 |
| PUT | `/api/auth/admin/profile` | ADMIN | 更新管理员资料 |
| PUT | `/api/auth/admin/password` | ADMIN | 修改管理员密码 |
| GET | `/api/auth/admin/users` | ADMIN | 用户列表（分页+搜索） |
| GET | `/api/auth/admin/users/{id}` | ADMIN | 用户详情 |
| POST | `/api/auth/admin/users` | ADMIN | 创建用户 |
| PUT | `/api/auth/admin/users/{id}` | ADMIN | 更新用户资料 |
| PUT | `/api/auth/admin/users/{id}/enabled` | ADMIN | 启用/禁用用户 |
| DELETE | `/api/auth/admin/users/{id}` | ADMIN | 删除用户 |
| GET | `/api/auth/admin/admins` | SUPER_ADMIN | 管理员列表 |
| POST | `/api/auth/admin/admins` | SUPER_ADMIN | 创建管理员 |
| PUT | `/api/auth/admin/admins` | SUPER_ADMIN | 更新管理员 |
| DELETE | `/api/auth/admin/admins/{id}` | SUPER_ADMIN | 删除管理员 |

## 商品 `/api/products`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/products` | 公开 | 商品列表 `?keyword&categoryId&sort=default/new/sales/price&searchMode=fuzzy/exact&page&size` |
| GET | `/api/products/{id}` | 公开 | 商品详情。微服务当前返回单商品对象；前端会将 `imageUrl` 和 `paramsText` 规范化为详情页图片和参数结构。legacy 单体可返回更完整的商品扩展数据 |
| GET | `/api/products/admin/all` | ADMIN | 后台商品列表 |
| POST | `/api/products/admin` | ADMIN | 新增商品 |
| PUT | `/api/products/admin` | ADMIN | 编辑商品 |
| DELETE | `/api/products/admin/{id}` | ADMIN | 软删除（下架+清零库存） |
| DELETE | `/api/products/admin/{id}/force` | ADMIN | 物理删除 |
| POST | `/api/products/admin/import` | ADMIN | CSV 批量导入 |
| GET | `/api/products/admin/export` | ADMIN | Excel 导出 |

## 分类 `/api/categories`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/categories` | 公开 | 全部分类（树形） |
| POST | `/api/admin/categories` | ADMIN | 新增分类 |
| PUT | `/api/admin/categories` | ADMIN | 编辑分类 |
| DELETE | `/api/admin/categories/{id}` | ADMIN | 删除分类 |

## 购物车 `/api/cart`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/cart` | 登录 | 购物车列表 |
| POST | `/api/cart/items` | 登录 | 添加商品 `{ productId, skuId?, specText?, quantity }`，支持传 `skuId` 关联 SKU |
| PUT | `/api/cart/items` | 登录 | 按 `cartItemId` 或 `productId + specText` 修改数量 |
| DELETE | `/api/cart/items` | 登录 | 按 `cartItemId` 或 `productId + specText` 删除单项 |

## 订单 `/api/orders`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/api/orders` | 登录 | 创建订单 `{ addressId, couponId?, paymentMethod }` |
| GET | `/api/orders` | 登录 | 我的订单 `?status` |
| GET | `/api/orders/{id}` | 登录 | 订单详情（含物流轨迹） |
| PUT | `/api/orders/{id}/pay` | 登录 | 模拟支付 |
| PUT | `/api/orders/{id}/pay-gateway` | 登录 | 模拟支付网关支付 |
| PUT | `/api/orders/{id}/cancel` | 登录 | 取消订单 |
| PUT | `/api/orders/{id}/confirm` | 登录 | 确认收货 |
| PUT | `/api/orders/{id}/refund` | 登录 | 申请退款 |
| GET | `/api/orders/{id}/logistics` | 登录 | 物流轨迹 |
| GET | `/api/admin/orders` | ADMIN | 全部订单 `?status&keyword` |
| PUT | `/api/admin/orders/{id}/ship` | ADMIN | 发货 |
| PUT | `/api/admin/orders/{id}/refund/approve` | ADMIN | 退款处理 |
| PUT | `/api/admin/orders/{id}/status` | ADMIN | 修改订单状态 |
| GET | `/api/admin/orders/export` | ADMIN | Excel 导出 |

## 地址 `/api/addresses`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/addresses` | 登录 | 地址列表 |
| POST | `/api/addresses` | 登录 | 新增地址 |
| PUT | `/api/addresses` | 登录 | 编辑地址 |
| DELETE | `/api/addresses/{id}` | 登录 | 删除地址 |
| PUT | `/api/addresses/{id}/default` | 登录 | 设为默认 |

## 首页 `/api/home`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/home` | 公开 | 首页数据（轮播图、热销、新品、公告） |
| GET | `/api/home/banners` | 公开 | 轮播图列表 |
| POST | `/api/home/banners` | ADMIN | 新增轮播图 |
| PUT | `/api/home/banners` | ADMIN | 编辑轮播图 |
| DELETE | `/api/home/banners/{id}` | ADMIN | 删除轮播图 |

## 评价 `/api/reviews`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/reviews?productId=` | 公开 | 商品评价列表 |
| POST | `/api/reviews` | 登录 | 发表评价 `{ productId, rating, content, imageUrl? }` |
| GET | `/api/reviews/admin/all` | ADMIN | 后台评价列表 |
| DELETE | `/api/reviews/admin/{id}` | ADMIN | 删除评价 |

## 收藏 `/api/favorites`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/favorites` | 登录 | 收藏列表 |
| POST | `/api/favorites/{productId}` | 登录 | 添加收藏 |
| DELETE | `/api/favorites/{productId}` | 登录 | 取消收藏 |
| GET | `/api/favorites/{productId}/status` | 登录 | 查询收藏状态 |

## 营销 `/api/marketing`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/marketing/specs/{productId}` | 公开 | 商品规格 |
| GET | `/api/marketing/coupons` | 公开 | 可用优惠券列表 |
| GET | `/api/marketing/coupons/user/{userId}` | 登录 | 我的优惠券 |
| POST | `/api/marketing/coupons/{couponId}/claim` | 登录 | 领取优惠券 |
| GET | `/api/marketing/promotions` | 公开 | 促销活动列表 |
| GET | `/api/marketing/admin/promotions` | ADMIN | 后台促销列表 |
| POST | `/api/marketing/admin/promotions` | ADMIN | 创建促销 |
| PUT | `/api/marketing/admin/promotions` | ADMIN | 编辑促销 |
| DELETE | `/api/marketing/admin/promotions/{id}` | ADMIN | 删除促销 |

## 反馈 `/api/feedback`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/feedback` | 登录 | 我的反馈 |
| POST | `/api/feedback` | 登录 | 提交反馈 `{ type, content, contact? }` |
| GET | `/api/admin/feedback` | ADMIN | 全部反馈 |
| PUT | `/api/admin/feedback` | ADMIN | 回复反馈 |
| PUT | `/api/admin/feedback/{id}/processed` | ADMIN | 标记已处理 |

## 咨询 `/api/consultations`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/consultations` | 登录 | 我的咨询 |
| POST | `/api/consultations` | 登录 | 提交咨询 `{ subject, content }` |
| GET | `/api/admin/consultations` | ADMIN | 全部咨询 |
| PUT | `/api/admin/consultations` | ADMIN | 回复咨询 |
| PUT | `/api/admin/consultations/{id}/processed` | ADMIN | 标记已处理 |

## 公告 `/api/announcements`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/announcements` | 公开 | 公告列表 |
| POST | `/api/admin/announcements` | ADMIN | 新增公告 |
| PUT | `/api/admin/announcements` | ADMIN | 编辑公告 |
| DELETE | `/api/admin/announcements/{id}` | ADMIN | 删除公告 |

## 活动通知 `/api/activity-notices`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/activity-notices` | 公开 | 活动通知列表 |
| GET | `/api/admin/activity-notices` | ADMIN | 后台活动通知列表 |
| POST | `/api/admin/activity-notices` | ADMIN | 新增 |
| PUT | `/api/admin/activity-notices` | ADMIN | 编辑 |
| DELETE | `/api/admin/activity-notices/{id}` | ADMIN | 删除 |

## 文件上传 `/api/files`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/api/files/upload` | 登录 | 上传图片（jpg/png/gif/webp，最大 5MB） |

## 管理仪表盘 `/api/admin`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/admin/dashboard` | ADMIN | 仪表盘数据（用户数、订单数、销售额、图表） |
| GET | `/api/admin/dashboard/export` | ADMIN | Excel 导出 |

## 遗留端点

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/legacy/status` | 公开 | JSP 渲染的系统状态页 |
| GET | `/legacy/servlet/status` | 公开 | JSON 格式系统状态 |
