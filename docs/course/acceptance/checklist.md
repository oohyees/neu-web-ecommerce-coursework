# 2026 指导书验收清单

## 当前验证状态

更新时间：2026-06-06。

已通过的自动化验证：

| 验证项 | 命令/方式 | 当前结果 |
| --- | --- | --- |
| 后端评分点测试 | Docker MySQL/Redis/MailHog 启动后执行 `mvn test` | 71 tests, 0 failures, 0 errors |
| 前端生产构建 | `npm --prefix frontend/shop-web run build`、`npm --prefix frontend/admin-web run build` | 通过 |
| 单体快速冒烟 | `./scripts/acceptance_check.sh` | 通过 |
| 单体完整接口冒烟 | `python3 scripts/acceptance_api_smoke.py` | 8 组流程全部 PASS |
| 微服务专项冒烟 | `scripts/microservices_smoke_test.sh` | Nacos/Gateway/Feign/库存扣减全部 PASS |
| 微服务网关完整接口冒烟 | `ACCEPTANCE_BASE_URL=http://127.0.0.1:18090/api ACCEPTANCE_MAILHOG_URL=http://127.0.0.1:18199 python3 scripts/acceptance_api_smoke.py` | 8 组流程全部 PASS |

已完成的人工巡检：

| 巡检项 | 入口/方式 | 当前结果 |
| --- | --- | --- |
| 单体用户端首页、商品列表、商品详情 | `http://localhost:18081`、`/products`、`/products/1` | 页面可访问，商品列表可正常加载商品和分页 |
| 购物车、结算、支付、订单 | 登录 `alice / 123456` 后访问 `/cart`、`/checkout`、`/pay/:id`、`/user/orders` | 购物车勾选、结算、模拟扫码支付、订单状态流转可用 |
| 管理后台核心页面 | 登录 `admin / admin123` 后访问 `/admin/dashboard`、`/admin/products`、`/admin/orders`、`/admin/users` | 后台登录、看板、商品、订单、用户列表可用 |
| 响应式布局 | 390px、768px 视口 | 用户端可浏览，后台切换为移动菜单按钮，抽屉导航可打开 |

人工巡检备注：当前数据库包含自动化冒烟和导入测试留下的 QA 用户、QA 分类、测试商品、测试订单、测试地址等数据；最终课堂演示或重新截图前应重置为干净种子数据。

问题记录：本轮人工巡检发现的问题、影响和修复结果见 [manual-qa-issues.md](manual-qa-issues.md)。

## 指导书正文逐条覆盖矩阵

### 提交成果物

| 指导书要求 | 覆盖方式 | 状态 |
| --- | --- | --- |
| 工程压缩包只打包源码及配置文件，总大小不超过 50M | `scripts/create_submission_zip.sh` 只打包 `frontend/backend/deploy/docs/scripts/pom.xml/README`，排除 `node_modules`、`target`、`dist`、`uploads`、`logs`、`docs/archive`、`.npm-cache` 等；本轮试打包大小约 4.9M | 已覆盖 |
| 工程压缩包命名格式 | 脚本默认参数为 `学号-姓名-班级-大作业-工程压缩包.zip` | 已覆盖 |
| 实验报告命名格式 | `docs/course/实验报告.md` 为报告正文源，导出 docx 时按 `学号-姓名-班级-大作业-实验报告.docx` 命名 | 已覆盖 |
| 报告包含实验目的、内容、环境、过程与分析、创新点、总结 | `docs/course/实验报告.md` 已按这些章节组织 | 已覆盖 |

### 课程技术目标

| 指导书正文点 | 覆盖方式 | 证据 |
| --- | --- | --- |
| Java 语言 | Spring Boot 单体、Spring Cloud 微服务、JUnit 测试 | `backend/legacy-web`、`backend/*`、`EcommerceScoringTests` |
| Web 前端 HTML/JavaScript/CSS | Vue 3 单页应用、Vite 构建、响应式 CSS | `frontend/shop-web/src`，`npm run build` 通过 |
| Servlet/JSP/Listener/Filter/JDBC | legacy 模块保留传统 Web 技术证据 | `/legacy/status`、`/legacy/servlet/status`、`Legacy*` 类 |
| MySQL 数据库 | 21 张业务表和种子数据 | `schema.sql`、`data.sql` |
| Spring、Vue3、MyBatis | 单体 Spring Boot + MyBatis，前端 Vue3 | `backend/legacy-web`、`frontend/shop-web` |
| 联合调试和系统测试 | 后端测试、接口冒烟、微服务冒烟、截图证据 | 测试命令与 `docs/course/acceptance/evidence` |

### 客户端基础功能

| 模块 | 指导书细项 | 覆盖方式 | 证据 |
| --- | --- | --- | --- |
| 商品 | 分类浏览、按分类筛选商品列表 | 分类表含一级/二级字段，商品列表支持 `categoryId` | `/products`、`GET /api/products?categoryId=` |
| 商品 | 商品详情页：图片、名称、价格、库存、参数、详情介绍 | 商品详情页展示基础信息、参数、详情、规格和评价 | `/products/:id`、`GET /api/products/{id}` |
| 商品 | 精准搜索、模糊搜索 | `searchMode=exact/fuzzy` | `ProductList.vue`、`ProductMapper.xml` |
| 商品 | 排序：价格/销量/新品 | 商品列表支持 sort | `/api/products?sort=price_asc/sales_desc/newest` |
| 商品 | 收藏/取消收藏、收藏列表 | 收藏接口和用户收藏页 | `/favorites`、`/user/favorites` |
| 商品 | 查看评价、提交评价、上传评价图片 | 评价列表、评价提交、图片上传 | `/reviews`、`/files/upload` |
| 购物车 | 选择规格、数量加入购物车 | 商品详情选择规格和数量，加购写入后端 | `/cart/items` |
| 购物车 | 列表展示图片、名称、单价、数量、小计 | 购物车表格和移动卡片展示 | `/cart` |
| 购物车 | 修改数量、删除商品 | 购物车步进器和删除按钮 | `PUT/DELETE /api/cart/items` |
| 购物车 | 勾选商品、全选/反选 | 前端选中集合、全选、反选、选中项结算 | `CartView.vue` |
| 购物车 | 结算跳转订单确认页 | 选中项进入 `/checkout` | `CartView.vue`、`CheckoutView.vue` |
| 订单 | 选择地址、商品清单、总价计算 | 订单确认页展示地址、商品、优惠券、总价 | `/checkout` |
| 订单 | 生成订单号、选择支付方式 | 下单接口生成订单号，支付页选择方式 | `POST /api/orders`、`/pay/:id` |
| 订单 | 模拟支付 | 支付接口更新支付状态和物流 | `PUT /api/orders/{id}/pay` |
| 订单 | 全部/待支付/待发货/待收货/已完成/已取消 | 我的订单状态 Tab | `/orders`、`GET /api/orders?status=` |
| 订单 | 取消、确认收货、申请退款、查看物流 | 订单操作按钮和物流弹窗 | `/orders/{id}/cancel`、`confirm`、`refund`、`logistics` |

### 管理后台基础功能

| 模块 | 指导书细项 | 覆盖方式 | 证据 |
| --- | --- | --- | --- |
| 管理员认证 | 管理员登录、权限验证 | 管理员登录返回 ADMIN/SUPER_ADMIN token，普通用户访问后台被拒绝 | `/admin/login`、鉴权冒烟 |
| 管理员认证 | 管理员退出 | 后台布局退出清理 session | `AdminLayout.vue`、`/api/auth/logout` |
| 管理员认证 | 角色管理/权限分级（可选） | SUPER_ADMIN 管理管理员账号，网关/拦截器限制超管接口 | `/admin/admins`、`/api/auth/admin/admins` |
| 数据看板 | 总用户、总订单、总销售额、今日订单/销售额 | 看板统计接口和卡片 | `/admin/dashboard` |
| 数据看板 | 销量趋势、热销排行、订单状态统计 | ECharts 折线/柱状/饼图 | `AdminDashboard.vue` |
| 用户管理 | 用户列表、搜索 | 后台用户列表分页搜索 | `/admin/users`、`/api/auth/admin/users` |
| 用户管理 | 禁用/启用、查看详情 | 用户状态修改和详情接口 | `/api/auth/admin/users/{id}/enabled`、`/{id}` |
| 商品管理 | 分类新增/修改/删除/排序 | 分类后台和 `sortOrder` | `/admin/categories` |
| 商品管理 | 商品列表、搜索、筛选 | 商品后台表格、分类/关键词筛选 | `/admin/products` |
| 商品管理 | 新增商品、上传商品图片、填写详情 | 商品 Drawer 表单和上传 | `/api/products/admin`、`/api/files/upload` |
| 商品管理 | 编辑、上下架、调整库存/价格 | 商品编辑表单和上下架按钮 | `AdminProducts.vue` |
| 商品管理 | 逻辑删除/物理删除 | 下架清库存和 force 删除接口 | `/api/products/admin/{id}`、`/force` |
| 商品管理 | 评价管理、删除违规评价 | 后台评价列表和删除 | `/admin/reviews`、`/api/reviews/admin/all` |
| 订单管理 | 订单列表、按订单号/用户/状态筛选 | 后台订单页和查询参数 | `/admin/orders`、`/api/admin/orders` |
| 订单管理 | 订单详情：商品、地址、支付、状态 | 订单详情接口返回 order/items/address/logistics | `/api/orders/{id}` |
| 订单管理 | 发货、取消、处理退款、修改状态 | 后台订单操作接口 | `/api/admin/orders/{id}/ship`、`refund/approve`、`status` |
| 订单管理 | Excel 导出 | POI 输出 xlsx | `/api/admin/orders/export` |

### 进阶要求

| 指导书细项 | 覆盖方式 | 证据 |
| --- | --- | --- |
| Element Plus | 表格、表单、弹窗、分页、上传、Drawer、Message | `frontend/shop-web/package.json`、各后台页面 |
| Pinia | 登录态、用户/管理员 session | `frontend/shop-web/src/store.js` |
| 认证鉴权 | Redis token session、前端路由守卫、后端拦截器、Gateway 鉴权 | `AuthInterceptor`、`AuthGatewayFilter` |
| OAuth 或 Shiro 可选 | Shiro profile 保留 `ShiroRealm`、`ShiroConfig` | `--spring.profiles.active=shiro` |
| 界面简洁美观、操作流畅、布局统一 | 前台购买流、后台表格化管理、统一布局组件 | 截图证据、`npm run build` |
| 明确操作反馈，避免错乱/失效 | Element Plus 消息、确认弹窗、空状态、响应式截图 | 页面截图和 UI 组件 |
| 邮箱注册、验证码验证 | 后端生成验证码写 Redis，通过 SMTP 发给 MailHog，注册一次性校验 | `/api/auth/code`、`/api/auth/register/email` |
| 用户登录、记住密码、退出 | 登录页记住密码，token session，退出销毁 | `UserLogin.vue`、`SessionService` |
| 找回密码、重置密码 | RESET 验证码 + 密码重置 | `/api/auth/password/reset` |
| 个人信息修改：头像、昵称、邮箱、密码 | 资料页、安全页、上传头像、改密码 | `/user/profile`、`/user/security` |
| 首页轮播 | Banner 表和首页轮播 | `/api/home`、`/api/home/banners` |
| 分类导航一级/二级 | `product_category.parent_id` 和分类导航/筛选 | `data.sql`、分类接口 |
| 热门、新品、促销商品 | 首页 hot/new，详情页促销，促销管理 | `/api/home`、`/api/marketing/promotions` |
| 搜索框、关键词搜索、热门搜索词 | 首页/列表搜索框和热词入口 | `HomeView.vue`、`ProductList.vue` |
| 收货地址新增、列表、修改、删除、默认 | 地址页和下单确认页 | `/api/addresses` |
| 规格、优惠券、秒杀/促销 | 商品规格、用户券、促销活动、秒杀库存 | `/api/marketing/*` |
| 客服咨询/在线聊天 | 咨询留言、管理员回复、WebSocket 在线聊天 | `/consultations`、`/ws/chat/{userId}` |
| 系统公告、活动通知 | 前台展示，后台 CRUD | `/api/announcements`、`/api/activity-notices` |
| 用户反馈 | 用户提交、管理员回复、标记处理 | `/api/feedback`、`/api/admin/feedback` |
| 轮播广告管理 | 列表/搜索、新增/编辑/删除、上传、链接、排序 | `/admin/banners` |
| 公告管理 | 发布/编辑/删除 | `/admin/announcements` |
| 反馈管理 | 查看、回复、标记已处理 | `/admin/feedback` |
| 管理员个人中心 | 修改资料、修改密码 | `/admin/profile` |
| 数据统计导出 | 看板导出 xlsx | `/api/admin/dashboard/export` |
| 商品批量导入/导出 | CSV 导入，xlsx 导出 | `/api/products/admin/import`、`/export` |
| 权限分级管理 | SUPER_ADMIN/ADMIN，超管管理管理员 | `/admin/admins` |
| 响应式布局 | 390px、768px 截图，后台移动抽屉菜单 | `evidence-responsive-*.png`、`AdminLayout.vue` |
| 图片上传 | 商品图、轮播图、头像、评价图上传 | `/api/files/upload` |
| 数据分页 | 商品、用户、订单、评价、反馈、咨询等列表分页 | `page/size` 参数和前端分页 |

### 拓展要求

| 指导书细项 | 覆盖方式 | 证据 |
| --- | --- | --- |
| Redis 缓存热点数据 | 首页、商品详情缓存；会话、验证码、节流存 Redis | `@Cacheable`、`SessionService`、Redis 配置 |
| 微服务拆分商品/订单/用户核心模块 | gateway、auth、product、order、admin 多服务独立构建部署 | `backend/*`、`deploy/docker-compose.yml` |
| 服务间高效通信 | Gateway + Nacos + OpenFeign，订单服务调用商品服务扣库存 | `microservices_smoke_test.sh` Feign 日志证据 |
| Docker 容器化部署 | 单体栈和微服务栈 Compose | `deploy/docker-compose.legacy.yml`、`deploy/docker-compose.yml` |
| 多环境一致性 | MySQL、Redis、MailHog、Nacos、后端、前端均容器化 | Docker 截图和启动脚本 |

## 评分表映射

| 分类 | 得分点 | 状态 | 前端页面 | 后端接口/实现 | 演示要点 |
| --- | --- | --- | --- | --- | --- |
| 基础 | 商品 | 已覆盖 | `/products`, `/products/:id` | `GET /api/products?keyword&categoryId&sort&searchMode` | 分类、精准/模糊搜索、排序、详情、规格、收藏、评价 |
| 基础 | 购物车 | 已覆盖 | `/cart` | `POST/PUT/DELETE /api/cart/items`, `GET /api/cart` | 规格数量加购、列表、小计、全选/反选、结算 |
| 基础 | 订单 | 已覆盖 | `/checkout`, `/orders`, `/pay/:id` | `POST/GET/PUT /api/orders` | 确认、提交、支付、状态 Tab、取消/收货/退款/物流 |
| 基础 | 管理员 | 已覆盖 | `/admin/login`, `/admin/dashboard` | `POST /api/auth/admin/login` | ADMIN/SUPER_ADMIN、退出、权限隔离 |
| 基础 | 数据 | 已覆盖 | `/admin/dashboard` | `GET /api/admin/dashboard` | 核心统计和 ECharts 图表 |
| 基础 | 用户 | 已覆盖 | `/admin/users` | `GET/PUT /api/auth/admin/users` | 搜索、分页、启停、详情 |
| 基础 | 后台商品 | 已覆盖 | `/admin/products`, `/admin/categories`, `/admin/reviews` | `/api/products/admin`, `/api/admin/categories`, `/api/reviews/admin/all` | 商品/分类/评价管理 |
| 基础 | 后台订单 | 已覆盖 | `/admin/orders` | `/api/admin/orders` | 筛选、详情、发货、退款、状态、导出 |
| 进阶 | 前端/后端/界面 | 已覆盖 | 全站 | Vue3、Element Plus、Pinia、Spring Boot、MyBatis、Redis 鉴权 | 构建通过、截图覆盖 |
| 进阶 | 客户端/后台/其它 | 已覆盖 | 用户中心、首页、地址、营销、系统管理 | 认证、首页、地址、规格优惠券、轮播、系统、导入导出、响应式、上传、分页 | 报告和截图覆盖 |
| 拓展 | 缓存/微服务/容器 | 已覆盖 | Docker/微服务截图 | Redis、Gateway、Nacos、Feign、Docker Compose | 自动化验收通过 |

## 满绩补齐记录

| # | 指导书原文 | 补齐方式 |
|---|-----------|----------|
| 1 | 精准搜索、模糊搜索 | `searchMode=exact` 精确匹配 + `searchMode=fuzzy` LIKE 模糊 |
| 2 | 逻辑删除 / 物理删除 | 软删除 `DELETE /api/products/admin/{id}` + 物理删除 `DELETE /api/products/admin/{id}/force` |
| 3 | 模拟支付 / 对接支付接口 | MOCK_PAY + `/pay/:id` 支付页 + `/api/orders/{id}/pay` |
| 4 | 可选用 OAuth 或 Shiro 框架 | Apache Shiro：`ShiroRealm` + `ShiroConfig`，`--spring.profiles.active=shiro` |
| 5 | 简单留言 / 在线聊天 | REST 留言 + WebSocket `/ws/chat/{userId}` 在线聊天 |
| 6 | 角色管理 + 权限分级管理 | `AdminAdmins.vue` 管理员账号 CRUD，SUPER_ADMIN 专用路由和后端鉴权 |
