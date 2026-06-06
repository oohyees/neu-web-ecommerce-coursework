# Ecommerce Platform

《Web 开发技术》电商平台大作业。仓库已整理为微服务优先的团队式 monorepo：默认架构使用 Spring Cloud Gateway、Nacos 和 OpenFeign 落实服务拆分、注册发现、统一鉴权和跨服务下单链路；`legacy-web` 保留完整单体业务与 Servlet/JSP/Listener/Filter/JDBC 课程证据。

## Repository Layout

```text
frontend/
  shop-web/            Vue 3 + Vite 商城前台
  admin-web/           Vue 3 + Vite 管理后台
backend/
  gateway-service/     Spring Cloud Gateway 微服务统一入口
  auth-service/        认证与会话服务
  product-service/     商品/分类/库存服务
  order-service/       购物车与订单服务
  admin-service/       后台聚合服务
  common/              公共响应与会话模型
  legacy-web/          单体回归与传统 Web 技术证据
deploy/
  docker-compose.yml         默认微服务栈
  docker-compose.legacy.yml  legacy 单体对照栈
docs/
  course/              课程资料
  course/acceptance/   验收清单、评分证据、截图材料
  archive/             历史记录，仅作追溯参考
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
- Deployment: Docker Compose, Nginx, Nacos, MailHog local SMTP capture
- Course evidence: Servlet, JSP, Listener, Filter, JDBC

## Run Microservice Stack

```bash
./scripts/build_microservices.sh
npm --prefix frontend/shop-web install
npm --prefix frontend/admin-web install
npm --prefix frontend/shop-web run build
npm --prefix frontend/admin-web run build
docker compose -f deploy/docker-compose.yml up -d --build
scripts/microservices_smoke_test.sh
```

URLs:

- Shop frontend: `http://localhost:18095`
- Admin frontend: `http://localhost:18082`
- Gateway: `http://localhost:18090`
- Auth service: `http://localhost:18091`
- Product service: `http://localhost:18092`
- Order service: `http://localhost:18093`
- Admin service: `http://localhost:18094`
- Nacos console: `http://localhost:18098/nacos`
- MailHog inbox: `http://localhost:18199`

The microservice smoke test checks Nacos registration, Gateway routing, Redis token authentication, `401/403` permission behavior, user/admin login, cart access, order creation, stock deduction, and Feign log evidence between `order-service` and `product-service`.

## Run Legacy Evidence Stack

```bash
mvn -q -DskipTests package
npm --prefix frontend/shop-web install
npm --prefix frontend/shop-web run build
docker compose -f deploy/docker-compose.legacy.yml up -d --build
./scripts/acceptance_check.sh
python3 scripts/acceptance_api_smoke.py
mvn test
```

URLs:

- Frontend: `http://localhost:18081`
- Backend API: `http://localhost:18080/api`
- MailHog inbox: `http://localhost:18099`
- Legacy status page: `http://localhost:18080/legacy/status`

Default accounts:

- User: `alice / 123456`
- Super admin: `admin / admin123`

Email verification is a real SMTP flow. Docker starts MailHog and configures the backend to send verification emails to it; the frontend never receives a fixed code from the API. For automated monolith smoke testing, `scripts/acceptance_api_smoke.py` reads the verification code from MailHog through `ACCEPTANCE_MAILHOG_URL` (`http://localhost:18099` by default).

`mvn test` expects the legacy Docker MySQL, Redis, and MailHog dependencies to be running; the legacy compose stack exposes them on `13306`, `6380`, and `11025`.

By default the smoke test creates a real demo order and deducts product stock. To run a non-mutating environment check, use:

```bash
SUBMIT_ORDER=0 scripts/microservices_smoke_test.sh
```

## Submission Package

```bash
./scripts/create_submission_zip.sh
```

The package excludes `node_modules`, `target`, `dist`, browser profiles, uploads, logs, `.git`, and local submission artifacts.
