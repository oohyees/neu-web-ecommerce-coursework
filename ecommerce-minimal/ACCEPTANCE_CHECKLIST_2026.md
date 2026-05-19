# 2026 指导书验收清单

## 测试状态：71 tests, 0 failures, 0 errors — BUILD SUCCESS (2026-05-19)

| 分类 | 得分点 | 状态 | 前端页面 | 后端接口 | 演示要点 |
| --- | --- | --- | --- | --- | --- |
| 基础 | 商品 | ✅ | `/products`, `/products/:id` | `GET /api/products?keyword&categoryId&sort&searchMode` | 9分类胶囊、精准/模糊搜索、综合/新品/销量/价格排序、秒杀价、规格选择、评价Tab |
| 基础 | 购物车 | ✅ | `/cart` | `POST/PUT/DELETE /api/cart/items`, `GET /api/cart` | 加购(规格+数量)、列表(图/名/价/量/计)、步进器、全选/反选、删除确认弹窗 |
| 基础 | 订单 | ✅ | `/checkout`, `/orders`, `/pay/:id` | `POST/GET/PUT /api/orders` | 地址卡片、优惠券、模拟支付+支付宝/微信/银联支付页面、6状态Tab、取消/收货/退款确认弹窗、物流轨迹 |
| 基础 | 管理后台 | ✅ | `/admin/login`, `/admin/dashboard` | `POST /api/auth/admin/login` | 深色主题登录、SUPER_ADMIN/ADMIN角色、Redis Session退出 |
| 基础 | 数据看板 | ✅ | `/admin/dashboard` | `GET /api/admin/dashboard` | 128用户/356订单/89240销售额、ECharts折线/饼图/柱图、待处理事项 |
| 基础 | 用户管理 | ✅ | `/admin/users` | `GET/PUT /api/auth/admin/users` | 分页列表、关键词搜索、启用/禁用确认弹窗、用户详情 |
| 基础 | 后台商品 | ✅ | `/admin/products`, `/admin/categories` | `POST/PUT/DELETE /api/products/admin`, `/api/admin/categories` | 商品CRUD、上下架、分类CRUD、软删除+物理删除、评价管理 |
| 基础 | 后台订单 | ✅ | `/admin/orders` | `GET/PUT /api/admin/orders` | 状态筛选、发货、退款处理、改状态、Excel导出 |
| 进阶 | 用户认证 | ✅ | `/register`, `/forgot-password`, `/profile` | `POST /api/auth/register/email`, `/code`, `/password/reset` | 邮箱验证码(10min有效期+1min节流)、记住密码、头像上传、资料修改、密码修改 |
| 进阶 | 首页 | ✅ | `/home` | `GET /api/home` | 三栏布局、轮播Banner、一级/二级分类、热门/新品、促销四宫格、优惠券领取、胶囊搜索+热词 |
| 进阶 | 地址 | ✅ | `/user/addresses` | `POST/PUT/DELETE /api/addresses`, `PUT default` | 收货人/手机/省市区/详细、设为默认、确认弹窗 |
| 进阶 | 规格与营销 | ✅ | `/admin/promotions` | `GET/POST/PUT/DELETE /api/marketing/admin/promotions` | 秒杀/直降/限时优惠CRUD、优惠券领取+用户券列表、商品规格(颜色/尺寸) |
| 进阶 | 客服咨询 | ✅ | `/consultations` | `GET/POST /api/consultations`, WebSocket `/ws/chat/{userId}` | 留言提交+实时WebSocket在线聊天、管理员回复+标记已处理 |
| 进阶 | 系统管理 | ✅ | `/admin/announcements`, `/admin/activity-notices`, `/admin/admins` | `/api/admin/announcements`, `/api/auth/admin/admins` | 公告CRUD、活动通知CRUD、管理员账号CRUD(SUPER_ADMIN专用) |
| 进阶 | 轮播 | ✅ | `/admin/banners` | `GET/POST/PUT/DELETE /api/home/banners` | 图片上传、链接设置、排序、搜索、确认弹窗 |
| 进阶 | 导入导出 | ✅ | 商品/订单/看板 | `GET /api/*/export`, `POST /api/products/admin/import` | 商品CSV导入、商品Excel导出、订单Excel导出、看板Excel导出 |
| 进阶 | 响应式/上传/分页 | ✅ | 全站 | `POST /api/files/upload` | @media 768px/1100px断点、图片上传、所有列表page/size分页 |
| 拓展 | Redis 缓存 | ✅ | 后台 | `@Cacheable` 首页+商品详情, Session存Redis, 验证码节流 | Redis CLI验证缓存键 |
| 拓展 | 微服务 | ✅ | `microservices/` | auth-service(8081), product-service(8082), order-service(8083) + Nginx(8088) | 三服务独立启动、网关转发 |
| 拓展 | Docker | ✅ | 根目录 + `microservices/` | docker-compose.yml × 2 | 主栈(单体+前端+MySQL+Redis)、微服务栈(三服务+网关+前端) |
| 拓展 | Shiro | ✅ | 后台 | `ShiroRealm` + `ShiroConfig` (`@Profile("shiro")` 激活) | 认证鉴权框架运用 |
| 拓展 | WebSocket | ✅ | 客服页 | `ChatWebSocketHandler` + `/ws/chat/{userId}` | 实时在线聊天 |

## 满绩补齐记录 (2026-05-19)

| # | 指导书原文 | 补齐方式 |
|---|-----------|----------|
| 1 | 精准搜索、模糊搜索 | `searchMode=exact` 精确匹配 + `searchMode=fuzzy` LIKE模糊 |
| 2 | 逻辑删除 / 物理删除 | 软删除 `DELETE /admin/{id}` + 物理删除 `DELETE /admin/{id}/force` |
| 3 | 模拟支付 / 对接支付接口 | MOCK_PAY + `PayView.vue` 支付宝/微信/银联三选一 + `/pay/:id` |
| 4 | 可选用OAuth或Shiro框架 | Apache Shiro 2.0.2: ShiroRealm + ShiroConfig, `--spring.profiles.active=shiro` |
| 5 | 简单留言 / 在线聊天 | REST留言 + WebSocket `/ws/chat/{userId}` 实时聊天 |
| 6 | (可选)角色管理 + 权限分级管理 | `AdminAdmins.vue` 管理员账号CRUD, SUPER_ADMIN 专用路由守卫 |
