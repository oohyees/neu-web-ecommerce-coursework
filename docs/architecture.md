# 电商平台架构文档

> 相关文档：[开发手册](development.md) · [API 参考](api-reference.md) · [部署说明](deployment.md)

## 一、项目概述

本仓库是一个课程电商平台，采用 **monorepo** 组织方式，支持**单体应用**和**微服务**两种部署模式。两种模式共享同一套 MySQL 数据库 schema 和初始数据，前端通过不同的 Nginx 配置反向代理到对应后端。

- **仓库地址**：`web/`
- **主分支**：`main`
- **语言/运行时**：Java 17、Node.js (Vue 3)

---

## 二、项目目录结构

```
web/
├── pom.xml                          # 根 Maven POM，聚合 7 个子模块
├── docker-compose.yml               # 单体模式 Docker 部署
├── docker-compose.microservices.yml # 微服务模式 Docker 部署
├── CLAUDE.md                        # 项目开发指南
│
├── apps/
│   ├── api/                         # Spring Boot 单体后端
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/java/com/example/ecommerce/
│   │       │   ├── EcommerceMinimalApplication.java
│   │       │   ├── controller/      # 16 个 REST Controller
│   │       │   ├── service/         # 4 个 Service
│   │       │   ├── mapper/          # 14 个 MyBatis Mapper 接口
│   │       │   ├── model/           # 16 个实体类
│   │       │   ├── common/          # 配置、拦截器、工具类
│   │       │   └── legacy/          # Servlet/JSP/Filter/JDBC 遗留模块
│   │       ├── main/resources/
│   │       │   ├── application.yml
│   │       │   ├── schema.sql       # 数据库 DDL
│   │       │   ├── data.sql         # 种子数据
│   │       │   └── mappers/         # 14 个 MyBatis XML
│   │       └── test/
│   │           └── java/.../EcommerceScoringTests.java  # 集成测试（~1200 行）
│   │
│   └── web/                         # Vue 3 + Vite 前端
│       ├── package.json
│       ├── vite.config.js
│       ├── Dockerfile               # 单体模式 Nginx 构建
│       ├── Dockerfile.microservices # 微服务模式 Nginx 构建
│       ├── nginx.conf
│       ├── nginx.microservices.conf
│       └── src/
│           ├── main.js              # 入口 + 路由守卫
│           ├── router.js            # 路由配置（33 个视图）
│           ├── store.js             # Pinia 状态管理
│           ├── api.js               # Axios HTTP 封装
│           ├── layouts/             # 3 个布局组件
│           ├── views/               # 33 个视图页面
│           └── components/          # 5 个公共组件
│
├── services/                        # Spring Cloud 微服务
│   ├── gateway/                     # API 网关 (Spring Cloud Gateway)
│   ├── auth-service/                # 认证服务
│   ├── catalog-service/             # 商品目录服务
│   ├── order-service/               # 订单服务
│   └── admin-service/               # 管理服务
│
├── libs/common/                     # 共享模型
│   └── src/.../
│       ├── ApiResponse.java         # 统一响应封装
│       └── SessionInfo.java         # 会话信息
│
├── docs/                            # 课程文档
│   ├── architecture.md              # 本文档
│   ├── acceptance/                  # 验收证据
│   ├── dev-log/                     # 开发日志
│   ├── report/                      # 实验报告
│   └── frontend-review/             # 前端审查
│
└── scripts/                         # 自动化脚本
    ├── acceptance_api_smoke.py      # 全流程验收测试（Python）
    ├── acceptance_check.sh          # 快速冒烟测试（Bash）
    ├── build_microservices.sh       # 微服务构建脚本
    ├── microservices_smoke_test.sh  # 微服务冒烟测试
    ├── create_submission_zip.sh     # 提交物打包
    └── build_real_catalog.py        # 商品数据生成
```

---

## 三、技术栈总览

### 3.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行语言 |
| Spring Boot | 3.2.4 | 应用框架 |
| Spring Cloud | 2023.0.1 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | Nacos 服务发现 |
| Spring Cloud Gateway | - | API 网关 |
| Spring Cloud OpenFeign | - | 服务间调用 |
| MyBatis / MyBatis-Spring | 3.x | ORM（仅单体） |
| MySQL Connector | - | 数据库驱动 |
| Redis (Lettuce) | - | 会话/缓存 |
| Spring Security Crypto | - | BCrypt 密码加密 |
| Apache Shiro | - | 可选权限框架（shiro profile） |
| Apache POI | - | Excel 导入导出 |
| Jakarta Servlet | 6.0 | 遗留模块 Servlet/Filter/Listener |
| JavaMail (Spring Mail) | - | 邮件发送 |

### 3.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5 | 核心框架 |
| Vite | 6 | 构建工具 |
| Vue Router | 4 | 客户端路由 |
| Pinia | 3 | 状态管理 |
| Element Plus | 2 | UI 组件库 |
| ECharts | 5 | 图表（管理后台仪表盘） |
| Axios | 1 | HTTP 客户端 |

### 3.3 基础设施

| 组件 | 版本 | 端口 (单体) | 端口 (微服务) |
|------|------|------------|--------------|
| MySQL | 8.4 | 13306 | 18096 |
| Redis | 7-alpine | 6380 | 18097 |
| Nacos | 2.3.2 | - | 18098 |
| MailHog | v1.0.1 | 11025 (SMTP), 18099 (UI) | 11125 (SMTP), 18199 (UI) |
| 后端 | - | 18080 | - |
| Gateway | - | - | 18090 |
| Auth Service | - | - | 18091 |
| Catalog Service | - | - | 18092 |
| Order Service | - | - | 18093 |
| Admin Service | - | - | 18094 |
| 前端 (Nginx) | - | 18081 | 18095 |

---

## 四、数据库设计

### 4.1 概览

- **数据库名**：`ecommerce_minimal`
- **字符集**：`utf8mb4`
- **表数量**：21 张表

### 4.2 表结构清单

#### 用户与认证（3 张）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `user` | 前端用户 | id, username(UNIQUE), password, nickname, email, phone, avatar_url, enabled |
| `admin_user` | 管理员 | id, username(UNIQUE), password, nickname, email, phone, role(ADMIN/SUPER_ADMIN) |
| `verification_code` | 邮箱验证码 | id, email, code, purpose, expires_at |

#### 商品与分类（3 张）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `product` | 商品 | id, category_id, name, price, stock, sales, is_on_sale, image_url, detail_html, params_text |
| `product_spec` | 商品规格 | id, product_id, spec_name, spec_value |
| `product_category` | 商品分类（树形） | id, parent_id(自引用), name, sort_order |

#### 营销（5 张）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `banner` | 首页轮播图 | id, title, image_url, link_url, sort_order |
| `announcement` | 公告 | id, title, content, created_at |
| `activity_notice` | 活动通知 | id, title, content, enabled, created_at |
| `coupon` | 优惠券模板 | id, name, threshold_amount, discount_amount, enabled |
| `user_coupon` | 用户优惠券 | id, user_id, coupon_id(UNIQUE pair), status, claimed_at, used_at |
| `promotion` | 促销/秒杀 | id, product_id, title, promotion_type, promotion_price, promotion_stock, start_at, end_at, enabled |

#### 交易（5 张）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `cart_item` | 购物车 | id, user_id, product_id, spec_text, quantity (UNIQUE: user+product+spec) |
| `user_address` | 收货地址 | id, user_id, receiver_name, phone, province, city, district, detail_address, is_default |
| `orders` | 订单主表 | id, order_no(UNIQUE), user_id, address_id, total_amount, status, payment_status, logistics_status, refund_status, coupon_id, discount_amount, payment_method, created_at |
| `order_item` | 订单明细 | id, order_id, product_id, product_name, spec_text, unit_price, quantity, subtotal |
| `order_logistics` | 物流记录 | id, order_id, content, created_at |

#### 互动（3 张）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `product_favorite` | 商品收藏 | id, user_id, product_id (UNIQUE pair) |
| `product_review` | 商品评价 | id, user_id, product_id, rating, content, image_url, created_at |
| `customer_consultation` | 客服咨询 | id, user_id, subject, content, reply, status, created_at |
| `feedback` | 用户反馈 | id, user_id, type, content, contact, reply, status, created_at |

---

## 五、单体架构详解

### 5.1 分层架构

```
┌─────────────────────────────────────────────────────────┐
│  Controller 层 (16 个 REST Controller)                   │
│  接收 HTTP 请求，参数校验，调用 Service                    │
├─────────────────────────────────────────────────────────┤
│  Service 层 (4 个 Service)                               │
│  业务逻辑：认证、订单、会话、邮件                          │
├─────────────────────────────────────────────────────────┤
│  Mapper 层 (14 个 MyBatis Mapper + XML)                  │
│  数据访问：SQL 映射                                       │
├─────────────────────────────────────────────────────────┤
│  Model 层 (16 个实体类)                                   │
│  数据模型 POJO                                           │
└─────────────────────────────────────────────────────────┘
```

### 5.2 Common 层基础设施

| 类 | 类型 | 功能 |
|----|------|------|
| `WebConfig` | @Configuration | CORS 配置、AuthInterceptor + ServiceScopeInterceptor 注册、静态资源映射 |
| `AuthInterceptor` | HandlerInterceptor | Token 认证：Redis 验证 Bearer token，公开路径白名单，admin/super_admin 角色控制 |
| `ServiceScopeInterceptor` | HandlerInterceptor | 运行时服务范围隔离（all/auth/product/order），模拟微服务拆分 |
| `ShiroConfig` | @Configuration @Profile("shiro") | Shiro 过滤器链，仅 shiro profile 激活时生效 |
| `ShiroRealm` | AuthorizingRealm @Profile("shiro") | 对 admin_user 和 user 表进行认证 |
| `CacheConfig` | @Configuration @EnableCaching | Redis 缓存配置，JSON 序列化 |
| `WebSocketConfig` | @Configuration @EnableWebSocket | WebSocket 端点注册 |
| `ChatWebSocketHandler` | TextWebSocketHandler | 客服聊天 WebSocket 处理器 |
| `SchemaMigration` | @Component @PostConstruct | 自动补全缺失的数据库列 |
| `ApiResponse` | record | `{success, message, data}` 统一响应 |
| `CurrentSession` | 工具类 | 从 request attribute 提取 SessionInfo |
| `SessionInfo` | record | `{Long id, String role}` |

### 5.3 Controller 清单

| # | Controller | 路径前缀 | 功能概述 |
|---|-----------|---------|---------|
| 1 | AuthController | `/api/auth` | 登录、注册、邮箱验证码、密码重置、个人资料、管理员登录、用户/管理员 CRUD |
| 2 | ProductController | `/api/products` | 商品列表（分类筛选/排序/分页）、商品详情、管理端 CRUD、CSV 导入、Excel 导出 |
| 3 | OrderController | `/api/orders`, `/api/admin/orders` | 创建订单、支付、取消、发货、退款、物流、管理端 CRUD |
| 4 | CartController | `/api/cart` | 购物车增删改查 |
| 5 | CategoryController | `/api/categories`, `/api/admin/categories` | 分类 CRUD |
| 6 | AddressController | `/api/addresses` | 收货地址 CRUD、设置默认 |
| 7 | FavoriteController | `/api/favorites` | 收藏/取消收藏、状态查询 |
| 8 | HomeController | `/api/home` | 首页数据：轮播图、热销、新品 |
| 9 | ReviewController | `/api/reviews` | 商品评价 CRUD |
| 10 | FileController | `/api/files` | 图片上传（jpg/png/gif/webp，最大 5MB） |
| 11 | MarketingController | `/api/marketing` | 商品规格、优惠券、促销活动、领取优惠券 |
| 12 | AnnouncementController | `/api/announcements`, `/api/admin/announcements` | 公告 CRUD |
| 13 | ActivityNoticeController | `/api/activity-notices`, `/api/admin/activity-notices` | 活动通知 CRUD |
| 14 | ConsultationController | `/api/consultations`, `/api/admin/consultations` | 客服咨询、管理员回复 |
| 15 | FeedbackController | `/api/feedback`, `/api/admin/feedback` | 用户反馈、管理员回复 |
| 16 | DashboardController | `/api/admin/dashboard` | 管理仪表盘：统计、图表数据、Excel 导出 |

### 5.4 Service 层

| Service | 功能 |
|---------|------|
| `AuthService` | 密码验证（BCrypt + 明文兼容迁移）、用户/管理员登录、CRUD、个人资料管理 |
| `OrderService` | 下单事务：校验购物车、检测秒杀、应用优惠券、扣减库存、创建订单、清空购物车 |
| `SessionService` | Redis Session：生成 UUID token、存储 SessionInfo、12 小时 TTL |
| `MailService` | 通过 Spring JavaMailSender 发送验证码邮件 |

### 5.5 MyBatis Mapper 层

14 个 Mapper 接口（`mapper/`），各自对应一个 XML 映射文件（`resources/mappers/`）：

| # | Mapper | 主要操作 |
|---|--------|---------|
| 1 | AuthMapper | 用户/管理员认证、CRUD、个人信息、密码 |
| 2 | ProductMapper | 商品列表/详情、管理端 CRUD、库存扣减 |
| 3 | OrderMapper | 订单/订单项/物流 CRUD、状态更新、搜索 |
| 4 | CartMapper | 购物车增删改、清空 |
| 5 | CategoryMapper | 分类 CRUD |
| 6 | AddressMapper | 地址 CRUD、设置默认 |
| 7 | HomeMapper | 轮播图、热销、新品查询 |
| 8 | ReviewMapper | 评价 CRUD |
| 9 | FavoriteMapper | 收藏切换、状态查询 |
| 10 | MarketingMapper | 规格、优惠券、促销、秒杀 |
| 11 | AnnouncementMapper | 公告 CRUD |
| 12 | ActivityNoticeMapper | 活动通知 CRUD |
| 13 | ConsultationMapper | 咨询 CRUD、回复、标记已处理 |
| 14 | FeedbackMapper | 反馈 CRUD、回复 |

### 5.6 遗留模块 (legacy/)

课程要求的传统 Java Web 技术栈证据模块：

| 类 | 类型 | 功能 |
|----|------|------|
| `LegacyAppListener` | ServletContextListener + HttpSessionListener | 记录应用启动时间和在线用户数 |
| `LegacyAuditFilter` | jakarta.servlet.Filter | 记录全部 API 请求及耗时（上限 50 条） |
| `LegacyStatusServlet` | HttpServlet | `/legacy/servlet/status` (JSON) 和 `/legacy/status` (HTML) |
| `LegacyJdbcDao` | @Repository | 原生 JDBC PreparedStatement 查询仪表盘统计 |
| `LegacyStatusController` | @Controller | MVC 控制器，返回 JSP 视图 |
| `LegacyWebConfig` | @Configuration | 注册 Servlet、Filter、Listener 和 JSP ViewResolver |

### 5.7 认证流程

```
用户登录
  │
  ▼
AuthController.login()
  │
  ▼
AuthService.login()
  ├── 查询 user 表
  ├── 密码验证 (BCrypt, 明文自动升级为 BCrypt)
  └── 返回 User 对象
  │
  ▼
SessionService.createSession(userId, role)
  ├── 生成 UUID token
  ├── Redis: SET session:{token} = "{userId}:{role}" EX 43200
  └── 返回 token
  │
  ▼
前端: localStorage.setItem("token", token)
  │
  ▼
后续请求: Authorization: Bearer <token>
  │
  ▼
AuthInterceptor.preHandle()
  ├── 公开路径白名单 → 放行
  ├── 从 Redis GET session:{token}
  ├── 解析 userId:role
  ├── 写入 request.setAttribute("sessionInfo", ...)
  ├── 管理路径检查 ADMIN/SUPER_ADMIN 角色
  └── 超管路径仅允许 SUPER_ADMIN
```

### 5.8 下单事务流程

```
OrderService.createOrder()
  │
  ├── 1. 校验购物车商品（存在性、归属用户）
  ├── 2. 校验收货地址（存在性、归属用户）
  ├── 3. 检测秒杀活动 → 自动应用促销价格
  ├── 4. 校验并应用优惠券 → 计算折扣金额
  ├── 5. 原子扣减商品库存 (UPDATE ... WHERE stock >= ?)
  ├── 6. 扣减秒杀活动库存
  ├── 7. 插入 orders 记录
  ├── 8. 批量插入 order_item 记录
  ├── 9. 插入 order_logistics 初始记录
  └── 10. 清空已购购物车项
```

---

## 六、微服务架构详解

### 6.1 架构图

```
                          ┌──────────────┐
                          │   浏览器      │
                          └──────┬───────┘
                                 │
                          ┌──────▼───────┐
                          │  Nginx :18095│  (前端静态文件)
                          │  /api → :18090│
                          └──────┬───────┘
                                 │
                          ┌──────▼───────┐
                          │ Gateway :18090│  Spring Cloud Gateway
                          │ AuthGatewayFilter│  全局鉴权 + 路由
                          └───┬───┬───┬──┘
                              │   │   │
          ┌───────────────────┤   │   ├───────────────────┐
          │                   │   │                       │
    ┌─────▼─────┐   ┌────────▼───▼──▼──────┐   ┌────────▼──────┐
    │Auth :18091│   │  Catalog :18092       │   │ Order :18093  │
    │ 认证服务   │   │  商品/分类/首页/营销    │   │ 购物车/订单    │
    │           │   │  评价/收藏/文件/公告    │   │ 地址          │
    └───────────┘   └──────────────────────┘   └───────┬───────┘
                                                      │ Feign
                                                ┌─────▼──────┐
                                                │ 内部调用     │
                                                │ /internal/  │
                                                └─────────────┘

                          ┌──────────────────┐
                          │ Admin :18094     │
                          │ 管理仪表盘        │
                          │ 用户/订单/反馈    │
                          │ 咨询管理          │
                          └──────────────────┘

                          ┌──────────────────┐
                          │ Nacos :18098     │
                          │ 服务注册/发现      │
                          └──────────────────┘
```

### 6.2 Gateway 路由规则

`AuthGatewayFilter` (GlobalFilter, Ordered=-100) 在鉴权后按以下规则转发：

| 请求路径 | 目标服务 | StripPrefix |
|---------|---------|-------------|
| `/api/auth/**` | ecommerce-auth-service | 1 |
| `/api/products/**` | ecommerce-catalog-service | 1 |
| `/api/categories/**` | ecommerce-catalog-service | 1 |
| `/api/home/**` | ecommerce-catalog-service | 1 |
| `/api/marketing/**` | ecommerce-catalog-service | 1 |
| `/api/reviews/**` | ecommerce-catalog-service | 1 |
| `/api/favorites/**` | ecommerce-catalog-service | 1 |
| `/api/files/**` | ecommerce-catalog-service | 1 |
| `/api/announcements/**` | ecommerce-catalog-service | 1 |
| `/api/activity-notices/**` | ecommerce-catalog-service | 1 |
| `/api/cart/**` | ecommerce-order-service | 1 |
| `/api/orders/**` | ecommerce-order-service | 1 |
| `/api/addresses/**` | ecommerce-order-service | 1 |
| `/api/feedback/**` | ecommerce-admin-service | 1 |
| `/api/consultations/**` | ecommerce-admin-service | 1 |
| `/api/admin/**` | ecommerce-admin-service | 1 |

### 6.3 Gateway 鉴权

`AuthGatewayFilter` 完整复刻单体的 AuthInterceptor 逻辑：

1. **公开路径白名单**：登录、注册、商品浏览（GET）、分类（GET）、首页等直接放行
2. **Token 校验**：从 Authorization header 提取 token，查询 Redis
3. **角色控制**：管理路径校验 ADMIN/SUPER_ADMIN 角色，超管路径仅 SUPER_ADMIN
4. **请求头注入**：通过后设置 `X-User-Id` 和 `X-Role` 请求头传递给下游服务

### 6.4 各微服务详述

#### Gateway (:18090)

- **框架**：Spring Cloud Gateway + LoadBalancer + Nacos Discovery
- **核心类**：`AuthGatewayFilter` (GlobalFilter)
- **Session 解析**：使用 StringRedisTemplate 读取 `session:{token}`，值为 `{userId}:{role}` 格式

#### Auth Service (:18091)

- **数据访问**：JDBC Template（不使用 MyBatis）
- **Controller**：`AuthController` (`/auth`)
- **功能**：所有认证相关端点（登录、注册、邮箱验证、密码重置、个人资料、管理员 CRUD）
- **额外依赖**：Spring Security Crypto (BCrypt)、Spring Mail、Redis

#### Catalog Service (:18092)

- **数据访问**：JDBC Template
- **Controller**：`ProductController`（~450 行大控制器）
- **功能**：
  - 公开：商品列表/详情、分类、首页、轮播图、公告、活动通知、规格、优惠券、促销、评价、收藏、文件上传
  - 管理：商品 CRUD、Excel 导入导出、促销管理、评价管理
  - 内部接口：`/internal/catalog/products/{id}/order-view` 和 `/internal/catalog/products/{id}/deduct-stock`（供 order-service 通过 Feign 调用）
- **额外依赖**：Apache POI (Excel)

#### Order Service (:18093)

- **数据访问**：JDBC Template
- **Controller**：`OrderController`
- **功能**：购物车 CRUD、地址 CRUD、订单 CRUD
- **跨服务调用**：通过 OpenFeign `CatalogClient` 调用 catalog-service 获取商品信息、扣减库存
- **上下文获取**：从 Gateway 注入的 `X-User-Id` 和 `X-Role` 读取用户身份

#### Admin Service (:18094)

- **数据访问**：JDBC Template
- **Controller**：
  - `AdminController` (`/admin`)：仪表盘、用户管理、订单管理（发货/退款/状态更新）、Excel 导出、分类/公告/活动通知 CRUD、反馈/咨询回复
  - `SupportController`：用户端反馈/咨询的创建和列表
- **额外依赖**：Apache POI (Excel)

### 6.5 跨服务通信

order-service → catalog-service 的 Feign 调用：

```
OrderService.createOrder()
  │
  ├── 调用 CatalogClient.orderView(productId)
  │     → GET /internal/catalog/products/{id}/order-view
  │     → 返回商品基本信息（名称、价格等）
  │
  └── 调用 CatalogClient.deductStock(productId, quantity)
        → POST /internal/catalog/products/{id}/deduct-stock
        → 原子扣减库存
```

---

## 七、前端架构

### 7.1 路由设计

**公开路由** (6 个)：首页、登录、注册、忘记密码、商品列表、商品详情

**用户路由** (11 个，需登录)：

| 路由 | 组件 | 说明 |
|------|------|------|
| `/cart` | CartView | 购物车 |
| `/checkout` | CheckoutView | 结算 |
| `/pay/:id` | PayView | 支付 |
| `/user/profile` | UserProfile | 个人资料 |
| `/user/orders` | UserOrders | 我的订单 |
| `/user/addresses` | UserAddress | 收货地址 |
| `/user/favorites` | UserFavorites | 我的收藏 |
| `/user/coupons` | UserCoupons | 我的优惠券 |
| `/user/security` | UserSecurity | 安全设置 |
| `/feedback` | FeedbackView | 意见反馈 |
| `/consultations` | ConsultationView | 客服咨询 |

**管理路由** (16 个，需管理员登录)：

| 路由 | 组件 | 说明 |
|------|------|------|
| `/admin/login` | AdminLogin | 管理员登录 |
| `/admin/dashboard` | AdminDashboard | 仪表盘 (ECharts) |
| `/admin/products` | AdminProducts | 商品管理 |
| `/admin/categories` | AdminCategories | 分类管理 |
| `/admin/orders` | AdminOrders | 订单管理 |
| `/admin/users` | AdminUsers | 用户管理 |
| `/admin/reviews` | AdminReviews | 评价管理 |
| `/admin/banners` | AdminBanners | 轮播图管理 |
| `/admin/announcements` | AdminAnnouncements | 公告管理 |
| `/admin/activity-notices` | AdminActivityNotices | 活动通知 |
| `/admin/promotions` | AdminPromotions | 促销管理 |
| `/admin/feedback` | AdminFeedback | 反馈管理 |
| `/admin/consultations` | AdminConsultations | 咨询管理 |
| `/admin/admins` | AdminAdmins | 管理员管理 |
| `/admin/profile` | AdminProfile | 管理员资料 |

### 7.2 路由守卫

在 `main.js` 中实现：
- 需要认证的路由：检查 localStorage 中 token 是否存在，无 token 跳转登录页
- 超管专属路由（管理员管理）：检查 role === 'SUPER_ADMIN'
- ADMIN 用户无法访问超管路由

### 7.3 状态管理

`useSessionStore` (Pinia)：

| State | 说明 |
|-------|------|
| userId | 当前用户 ID |
| nickname | 用户昵称 |
| adminId | 管理员 ID |
| role | 角色 (USER/ADMIN/SUPER_ADMIN) |

Actions：`setUser(data)`, `setAdmin(data)`, `logout()`

持久化策略：`localStorage`

### 7.4 HTTP 请求层

Axios 实例配置：
- Base URL：`import.meta.env.VITE_API_BASE_URL || '/api'`
- 请求拦截器：自动附加 `Authorization: Bearer <token>`（从 localStorage 读取）
- 响应拦截器：401 时自动清除登录态并跳转 `/login`

### 7.5 布局组件

| 布局 | 用途 | 结构 |
|------|------|------|
| ShopLayout | 商城前台 | Header + Sidebar/Nav + Main + Footer |
| UserLayout | 用户中心 | 嵌套子路由：个人资料、订单、地址等 |
| AdminLayout | 管理后台 | 侧边栏导航 + Header + Main |

### 7.6 公共组件

| 组件 | 用途 |
|------|------|
| ProductCard | 商品列表卡片 |
| AdminPageHeader | 管理页面标题/描述 |
| StatusTag | 订单状态标签 |
| MoneySummary | 金额汇总展示 |
| EmptyState | 空数据占位 |

---

## 八、部署与运维

### 8.1 单体模式启动

```bash
# 构建并启动
docker compose up -d --build

# 访问
# 前端: http://localhost:18081
# API:  http://localhost:18080/api
# 传统: http://localhost:18080/legacy/status
# MailHog: http://localhost:18099

# 验收测试
./scripts/acceptance_check.sh
```

### 8.2 微服务模式启动

```bash
# 构建所有微服务
./scripts/build_microservices.sh

# 启动
docker compose -f docker-compose.microservices.yml up -d --build

# 访问
# 前端: http://localhost:18095
# Nacos: http://localhost:18098/nacos

# 验收测试
./scripts/microservices_smoke_test.sh
```

### 8.3 默认账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 普通用户 | alice | 123456 |
| 超级管理员 | admin | admin123 |
| 管理员 | operator | admin123 |

### 8.4 测试脚本

| 脚本 | 语言 | 说明 |
|------|------|------|
| `acceptance_api_smoke.py` | Python | 全流程验收测试，8 组用例，任一失败即退出非零 |
| `acceptance_check.sh` | Bash | 9 步快速冒烟测试 |
| `microservices_smoke_test.sh` | Bash | 微服务专项测试，含 Feign 跨服务调用验证 |

---

## 九、关键设计决策

### 9.1 双模设计

- 单体（`apps/api`）包含全部业务代码，MyBatis 数据访问层
- 微服务版本将同样的业务按领域拆分，数据访问改用 JDBC Template
- 两套后端共享同一数据库 schema 和 seed data
- 前端通过不同的 Nginx 配置切换代理目标
- `ServiceScopeInterceptor` 在单体中模拟服务边界的路径隔离

### 9.2 无状态认证

- Redis-backed Token Session：服务端存储，客户端持有
- 12 小时 TTL，登出即删除 Redis key 立即失效
- 相比于 JWT，支持服务端主动失效
- Gateway 模式下，下游服务从请求头获取用户上下文而非再次查询 Redis

### 9.3 数据库共享策略

- 微服务模式下各服务访问同一 MySQL 实例，不需要分布式事务
- 通过 Feign 内部接口实现跨服务数据一致性（如库存扣减）
- 用户上下文由 Gateway 注入请求头，各服务不单独维护会话

### 9.4 遗留模块保留

- Servlet/Filter/Listener/JSP/JDBC 仅存在于单体应用中
- 作为课程要求的传统 Java Web 技术证据
- 不影响主线业务功能
