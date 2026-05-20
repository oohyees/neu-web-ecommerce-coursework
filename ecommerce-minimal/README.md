# Ecommerce Minimal

《Web 开发技术》电商平台大作业。

## 当前状态

截至 2026-05-18：

- 主业务代码已接近高分版本
- 本轮已真实复测：
  - 前端构建
  - 后端构建
  - Redis
  - 多组核心接口
  - 完整订单闭环
- 当前仍待完成：
  - 前端逐页浏览器验收
  - 当前机器上的 Docker / 微服务实机复验
  - 最终截图、报告、压缩包

## 技术栈

- 前端：Vue 3、Vite、Element Plus、Pinia、ECharts
- 后端：Spring Boot、MyBatis、Redis
- 数据库：MySQL
- 部署：Docker Compose

## 本地运行

### 新库

1. 执行：
   - `backend/src/main/resources/schema.sql`
   - `backend/src/main/resources/data.sql`
2. 启动后端：
   - `cd backend`
   - `mvn spring-boot:run`
3. 启动前端：
   - `cd frontend`
   - `npm install`
   - `npm run dev -- --host 127.0.0.1`

### 旧库升级

如果不是全新初始化的库，当前已知需要执行：

1. `backend/src/main/resources/migration_20260517.sql`
2. `backend/src/main/resources/migration_20260518_complete.sql`
3. `backend/src/main/resources/migration_20260518_seed.sql`

本轮已经用这套流程把旧库补到可运行状态。

## 默认账号

- 用户：`alice / 123456`
- 超级管理员：`admin / admin123`

## 真实邮件验证码

项目已支持真实 SMTP，运行时可通过环境变量注入：

- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`

若未配置，验证码接口会安全失败，不影响其余主流程联调。

## Docker 说明

当前后端镜像复用本地已打包好的 `backend/target/ecommerce-minimal-0.0.1-SNAPSHOT.jar`，因此在执行 `docker compose up -d --build` 前，需要先完成一次后端打包：

- `cd backend`
- `mvn -DskipTests package`

## 先读这些文档

1. `docs/dev-log/HANDOFF.md`
2. `docs/dev-log/PROJECT_STATE.md`
3. `docs/dev-log/GAP_LOG.md`
4. `docs/acceptance/ACCEPTANCE_REPORT.md`
5. `docs/acceptance/ACCEPTANCE_CHECKLIST_2026.md`
