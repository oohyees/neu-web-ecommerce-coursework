# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

《Web 开发技术》课程电商平台大作业，目标满绩现场验收。技术栈：Vue 3 + Element Plus + Pinia（前端）、Spring Boot 3.3 + MyBatis + MySQL + Redis（后端）、Docker Compose 部署。

## 常用命令

### 前端（`frontend/`）

```bash
npm install              # 安装依赖
npm run dev -- --host 127.0.0.1   # 开发服务器（端口 5173）
npm run build            # 生产构建 → dist/
```

### 后端（`backend/`）

```bash
mvn spring-boot:run                          # 启动（需先启动 MySQL + Redis）
mvn test                                     # 运行测试
mvn -DskipTests package                      # 打包（Docker 构建前必须执行）
```

### Docker

```bash
# 主栈（单体后端 + 前端 + MySQL + Redis）
docker compose up -d --build

# 微服务栈
cd microservices && docker compose up -d --build
```

### 数据库

- MySQL 连接：`127.0.0.1:3306`，库名 `ecommerce_minimal`，root / 123456
- Redis：`127.0.0.1:6380`（Docker 映射宿主机 6380→容器 6379）
- 全新初始化：顺序执行 `schema.sql` → `data.sql`
- 旧库升级：按顺序执行 `migration_20260517.sql` → `migration_20260518_complete.sql` → `migration_20260518_seed.sql`

### 默认账号

- 用户：`alice / 123456`
- 超级管理员：`admin / admin123`

## 架构

### 后端分层（`com.example.ecommerce`）

```
controller/   # REST 接口，路径 /api/*
service/      # 业务逻辑（AuthService, OrderService, MailService, SessionService）
mapper/       # MyBatis Mapper 接口 → resources/mappers/*.xml
model/        # 实体类（User, Product, OrderView, CartItemView 等）
common/       # ApiResponse、AuthInterceptor、ServiceScopeInterceptor、WebConfig
```

### 鉴权机制

- `AuthInterceptor` 拦截 `/api/**`，公开路径放行（GET 商品/分类/首页等），其余需 `Authorization: Bearer <token>` 头
- Session 存 Redis，token 为 UUID
- 角色：`SUPER_ADMIN` > `ADMIN`。`/api/admin/users`、`/api/admin/announcements`、`/api/admin/activity-notices` 仅 SUPER_ADMIN 可访问
- 前端路由守卫在 `main.js` 中，检查 `session.userId` / `session.adminId` / `session.role`

### 微服务（`microservices/`）

相同业务代码基线，通过 `APP_SERVICE_SCOPE` 环境变量控制实例职责：

| 服务 | 端口 | 范围 |
|------|------|------|
| auth-service | 8081 | `/api/auth`, `/api/admin/*`（部分） |
| product-service | 8082 | `/api/products`, `/api/categories`, `/api/home`, `/api/marketing` 等 |
| order-service | 8083 | `/api/orders`, `/api/cart`, `/api/addresses`, `/api/consultations` |

网关（Nginx）按路径转发，前端统一入口 `http://localhost:8088`。当前共享 MySQL 和 Redis。

### 前端结构

```
src/
├── api.js          # Axios 实例，baseURL 默认 localhost:8080/api，自动带 token
├── router.js       # 路由表，/admin/* 为后台，其他为客户端
├── store.js        # Pinia session store（userId, adminId, role），持久化到 localStorage
├── main.js         # 入口，注册 Pinia/Router/ElementPlus，路由守卫
├── styles.css      # 全局样式
├── layouts/        # ShopLayout.vue（客户端）、AdminLayout.vue（后台）
├── views/          # 页面组件（客户端 + 后台混放，按命名区分）
└── components/     # ProductCard, StatusTag, EmptyState 等复用组件
```

## 关键配置

- 后端配置：`backend/src/main/resources/application.yml`（数据源、Redis、MyBatis、SMTP）
- SMTP 通过环境变量注入：`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`。未配置时验证码接口返回错误但不影响主流程
- 前端 API 地址：通过 `VITE_API_BASE_URL` 环境变量覆盖，默认 `http://localhost:8080/api`

## 文档导航

接手后按顺序读：
1. `docs/dev-log/HANDOFF.md` — 交接说明，当前真实进度
2. `docs/dev-log/PROJECT_STATE.md` — 项目状态，已验证项与欠账
3. `docs/acceptance/ACCEPTANCE_CHECKLIST_2026.md` — 按评分点的验收清单
4. `docs/dev-log/GAP_LOG.md` — 已知未完成/暂缓项
5. `docs/acceptance/SCORING_MAP.md` — 评分点→演示入口映射

## 工作约束

- 功能只有两个状态：满绩完成 或 未完成，不存在 demo/初版 状态
- 不产生数字自评分数，只报告可验证的事实
- 不按衍生文档（历史自评报告等）开发，所有需求回指验收清单
- 新增功能必须带对应验证（测试或用实际接口调用确认）
- 禁止重做已完成的模块（客服咨询、活动通知、管理员分级、数据库迁移、微服务部署）
