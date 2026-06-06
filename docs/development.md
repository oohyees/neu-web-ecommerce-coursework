# 开发手册

> 相关文档：[架构总览](architecture.md) · [API 参考](api-reference.md) · [部署说明](deployment.md)

## 环境依赖

- Java 17
- Maven 3.8+
- Node.js 18+
- Docker + Docker Compose

## 项目结构速览

```
web/
├── backend/
│   ├── gateway-service/       # :18090 Spring Cloud Gateway + AuthGatewayFilter
│   ├── auth-service/          # :18091 认证
│   ├── product-service/       # :18092 商品/分类/首页/营销
│   ├── order-service/         # :18093 购物车/订单/地址（Feign → product）
│   ├── admin-service/         # :18094 管理后台
│   ├── common/                # ApiResponse, SessionInfo
│   └── legacy-web/            # legacy 单体 + Servlet/JSP/Filter/Listener/JDBC 课程证据
├── frontend/
│   ├── shop-web/              # 前台商城 Vue 工程
│   └── admin-web/             # 后台管理 Vue 工程
├── docker/                    # Docker Compose 部署配置
└── scripts/                   # 构建/验收脚本
```

## 常用命令

```bash
# 构建
mvn -q -DskipTests package          # 后端
npm --prefix frontend/shop-web run build
npm --prefix frontend/admin-web run build

# 测试
docker compose -f docker/docker-compose.legacy.yml up -d mysql redis mailhog
mvn test                            # 依赖 Docker MySQL/Redis/MailHog，71 个评分点测试

# 默认微服务栈（主演示路径）
./scripts/build_microservices.sh    # 构建全部微服务
docker compose -f docker/docker-compose.yml up -d --build
./scripts/microservices_smoke_test.sh

# legacy 单体对照栈（传统 Web 技术证据/fallback）
docker compose -f docker/docker-compose.legacy.yml up -d --build
./scripts/acceptance_check.sh       # 快速冒烟
```

> 各模式启动的具体服务和端口见 [部署说明](deployment.md)。

## 端口映射

### 单体模式

| 服务 | 端口 |
|------|------|
| 后端 API | 18080 |
| 前端 Nginx | 18081 |
| MySQL | 13306 |
| Redis | 6380 |
| MailHog UI | 18099 |

### 微服务模式

| 服务 | 端口 |
|------|------|
| Gateway | 18090 |
| Auth Service | 18091 |
| Product Service | 18092 |
| Order Service | 18093 |
| Admin Service | 18094 |
| 前台 Nginx | 18095 |
| 后台 Nginx | 18082 |
| MySQL | 18096 |
| Redis | 18097 |
| Nacos | 18098 |
| MailHog UI | 18199 |

## 默认账户

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 普通用户 | alice | 123456 | 有测试订单和购物车数据 |
| 超级管理员 | admin | admin123 | 全部管理权限 |
| 管理员 | operator | admin123 | 基础管理权限（无超管功能） |

## 认证机制

- 登录后服务端生成 UUID token → Redis `session:{token}` = `{userId}:{role}`，TTL 12h
- 客户端 `Authorization: Bearer <token>`
- AuthInterceptor（单体）或 AuthGatewayFilter（微服务）校验
- 登出删除 Redis key，token 立即失效
- 微服务模式下，Gateway 鉴权后注入 `X-User-Id` 和 `X-Role` 头给下游

> 完整认证序列图见 [架构文档 § 认证流程](architecture.md)。各端点鉴权要求见 [API 参考 § 鉴权说明](api-reference.md)。

## 配置

### 后端 `application.yml`

关键配置项（通过环境变量覆盖）：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SPRING_DATASOURCE_URL` | jdbc:mysql://127.0.0.1:13306/ecommerce_minimal... | 测试默认连接 Docker 暴露的 MySQL |
| `SPRING_DATA_REDIS_HOST/PORT` | 127.0.0.1:6380 | Redis |
| `MAIL_HOST/PORT/USERNAME/PASSWORD` | 127.0.0.1:11025 / noreply@example.test | Docker MailHog 本地 SMTP 捕获；生产可替换为真实邮箱服务 |

### 前端 `vite.config.js`

- Dev server: 5173
- API 代理目标由 `VITE_API_BASE_URL` 控制，默认 `/api`

## 已知限制

| 项目 | 说明 | 影响 |
|------|------|------|
| SMTP | Docker 环境使用 MailHog 捕获真实 SMTP 邮件；生产环境可替换为外部 SMTP | 本地验收不依赖外网邮箱，验证码不由后端返回固定值 |
| 微服务共享 MySQL | 所有微服务指向同一数据库实例 | 架构边界通过独立部署+网关转发证明，非功能缺陷 |
| 商品图片 | 当前运行库使用 `/catalog/...` 本地图片路径，前端 `public/catalog` 打包进 Nginx | 演示不依赖外网图片；DummyJSON 远程 URL 仅保留在 SQL 种子块中 |
| Shiro | `@Profile("shiro")` 控制，默认关闭 | Token+Redis+角色分级已覆盖鉴权全部需求，Shiro 作为框架运用证明可按需激活 |
| 客服 | 当前实现为留言/回复模式 | WebSocket 端点存在，可升级为实时聊天 |
