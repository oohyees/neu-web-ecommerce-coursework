# 部署说明

> 相关文档：[架构总览](architecture.md) · [开发手册](development.md) · [API 参考](api-reference.md)

## 单体模式

```bash
docker compose up -d --build
```

> 构建命令详见 [开发手册 § 常用命令](development.md)。

### 服务清单

| 服务 | 镜像 | 端口 |
|------|------|------|
| mysql | mysql:8.4 | 13306 |
| redis | redis:7-alpine | 6380 |
| mailhog | mailhog/mailhog:v1.0.1 | 11025 (SMTP), 18099 (UI) |
| backend | 本地构建 `./apps/api` | 18080 |
| frontend | 本地构建 `./apps/web` (Nginx) | 18081 |

### 验证

```bash
curl http://localhost:18080/api/home
curl http://localhost:18081
curl http://localhost:18080/legacy/status
./scripts/acceptance_check.sh
```

---

## 微服务模式

```bash
./scripts/build_microservices.sh
docker compose -f docker-compose.microservices.yml up -d --build
```

### 服务清单

| 服务 | 端口 | 说明 |
|------|------|------|
| nacos | 18098 | 注册中心/配置中心 |
| mysql | 18096 | 共享数据库 |
| redis | 18097 | 共享缓存/会话 |
| mailhog | 11125 (SMTP), 18199 (UI) | 邮件捕获 |
| gateway | 18090 | API 网关 + 全局鉴权 |
| auth-service | 18091 | 认证 |
| catalog-service | 18092 | 商品/分类/首页/营销 |
| order-service | 18093 | 购物车/订单/地址 |
| admin-service | 18094 | 管理后台 |
| frontend | 18095 | Nginx 静态文件 |

### 健康检查

```bash
# Nacos 服务列表
curl http://localhost:18098/nacos/v1/ns/healthy-instances?serviceName=ecommerce-gateway

# 通过网关访问各服务
curl http://localhost:18090/api/home
curl -X POST http://localhost:18090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123456"}'

# 完整冒烟测试
./scripts/microservices_smoke_test.sh
```

---

## 数据库

- 数据库名：`ecommerce_minimal`（utf8mb4）
- Schema：`apps/api/src/main/resources/schema.sql`（21 张表）
- 种子数据：`apps/api/src/main/resources/data.sql`（商品、用户、订单等演示数据）
- Docker 启动时自动执行 schema.sql 和 data.sql

### 手动连接

```bash
mysql -h 127.0.0.1 -P 13306 -u root -p123456 ecommerce_minimal
```

### 重置演示数据

自动化冒烟会创建 QA 用户、订单、商品、评价和上传记录。最终课堂演示或重新截图前，可将数据库恢复到 `schema.sql` + `data.sql` 的干净种子状态：

```bash
./scripts/reset_demo_database.sh
```

Windows PowerShell 环境可使用：

```powershell
./scripts/reset_demo_database.ps1 -Port 13306
```

---

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SPRING_DATASOURCE_URL` / `DB_URL` | Compose 内部 MySQL 地址 | 单体使用 `SPRING_DATASOURCE_URL`，微服务使用 `DB_URL` |
| `SPRING_DATASOURCE_USERNAME` | root | 数据库用户 |
| `SPRING_DATASOURCE_PASSWORD` | 123456 | 数据库密码 |
| `SPRING_DATA_REDIS_HOST` / `REDIS_HOST` | Compose 内部 Redis 地址 | 单体使用 Spring 配置名，网关/微服务使用简化变量 |
| `SPRING_DATA_REDIS_PORT` / `REDIS_PORT` | 6379 | Redis 端口 |
| `MAIL_HOST` | mailhog | SMTP 服务器 |
| `MAIL_PORT` | 1025 | SMTP 端口 |
| `MAIL_USERNAME` | - | 邮箱账号 |
| `MAIL_PASSWORD` | - | 邮箱密码/授权码 |
| `MAIL_FROM` | noreply@example.test | 发件人地址 |

Docker 默认使用 MailHog 作为本地 SMTP 捕获服务。验证码仍由后端生成、写入 Redis、通过 SMTP 发出，再由 MailHog UI 或验收脚本读取邮件内容；前端和接口不会返回固定验证码。
