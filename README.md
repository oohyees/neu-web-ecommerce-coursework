# Ecommerce Platform

《Web 开发技术》电商平台大作业。仓库已整理为团队式 monorepo：主业务保留单体栈作为对照与兜底，微服务工程使用 Spring Cloud Gateway、Nacos 和 OpenFeign 落实服务拆分、注册发现、统一鉴权和跨服务下单链路。

## Repository Layout

```text
apps/
  api/                 Spring Boot 主业务后端，含传统 Web 证据模块
  web/                 Vue 3 + Vite 前台/后台 SPA
services/
  gateway/             Spring Cloud Gateway 微服务统一入口
  auth-service/        认证与会话服务
  catalog-service/     商品/分类查询服务
  order-service/       购物车与订单服务
  admin-service/       后台聚合服务
libs/
  common/              公共响应与会话模型
docs/
  course/              课程资料
  report/              实验报告正文和素材索引
  acceptance/          验收清单、评分证据、截图材料
  dev-log/             开发记录
scripts/
  acceptance_check.sh
  microservices_smoke_test.sh
  build_microservices.sh
  create_submission_zip.sh
```

## Tech Stack

- Frontend: Vue 3, Vite, Element Plus, Pinia, ECharts
- Backend: Java 17, Spring Boot 3.2.4, Spring Cloud 2023.0.1, Spring Cloud Alibaba 2023.0.1.0, MyBatis, MySQL, Redis
- Microservices: Spring Cloud Gateway, Nacos Discovery, OpenFeign, Spring Cloud LoadBalancer
- Deployment: Docker Compose, Nginx, Nacos
- Course evidence: Servlet, JSP, Listener, Filter, JDBC

## Run Monolith Stack

```bash
mvn -q -DskipTests package
npm --prefix apps/web install
npm --prefix apps/web run build
docker compose up -d --build
./scripts/acceptance_check.sh
```

URLs:

- Frontend: `http://localhost:18081`
- Backend API: `http://localhost:18080/api`
- Legacy status page: `http://localhost:18080/legacy/status`

Default accounts:

- User: `alice / 123456`
- Super admin: `admin / admin123`

## Run Microservice Stack

```bash
./scripts/build_microservices.sh
docker compose -f docker-compose.microservices.yml up -d --build
scripts/microservices_smoke_test.sh
```

URLs:

- Frontend: `http://localhost:18095`
- Gateway: `http://localhost:18090`
- Auth service: `http://localhost:18091`
- Catalog service: `http://localhost:18092`
- Order service: `http://localhost:18093`
- Admin service: `http://localhost:18094`
- Nacos console: `http://localhost:18098/nacos`

The microservice smoke test checks Nacos registration, Gateway routing, Redis token authentication, `401/403` permission behavior, user/admin login, cart access, order creation, stock deduction, and Feign log evidence between `order-service` and `catalog-service`.

By default the smoke test creates a real demo order and deducts product stock. To run a non-mutating environment check, use:

```bash
SUBMIT_ORDER=0 scripts/microservices_smoke_test.sh
```

## Submission Package

```bash
./scripts/create_submission_zip.sh
```

The package excludes `node_modules`, `target`, `dist`, browser profiles, uploads, logs, `.git`, and local submission artifacts.
