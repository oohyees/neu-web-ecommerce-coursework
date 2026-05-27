# 微服务验收命令清单

本文档用于课堂验收和报告截图。微服务版本落实了 Spring Cloud Gateway、Nacos Discovery、Redis token 鉴权、OpenFeign 跨服务下单和库存扣减。

## 运行地址

| 服务 | 地址 |
| --- | --- |
| 前端 | `http://localhost:18095` |
| Gateway | `http://localhost:18090` |
| Nacos 控制台 | `http://localhost:18098/nacos` |
| Auth service | `http://localhost:18091` |
| Catalog service | `http://localhost:18092` |
| Order service | `http://localhost:18093` |
| Admin service | `http://localhost:18094` |

## 启动与构建

```bash
mvn -q -DskipTests package
npm --prefix apps/web run build
docker compose -f docker-compose.microservices.yml up -d --build
docker compose -f docker-compose.microservices.yml ps
```

## 自动化验收

完整验收会创建真实测试订单并扣减商品库存：

```bash
scripts/microservices_smoke_test.sh
```

通过标准：

```text
PASS stock deducted productId=...
MICROSERVICES SMOKE TEST PASSED
```

只检查环境、不创建订单时可运行：

```bash
SUBMIT_ORDER=0 scripts/microservices_smoke_test.sh
```

## 手动接口证据

```bash
curl -i http://127.0.0.1:18090/api/products
curl -i http://127.0.0.1:18090/api/cart
curl -s -X POST http://127.0.0.1:18090/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"123456"}'
```

预期结果：

| 检查项 | 预期 |
| --- | --- |
| `/api/products` | `200 OK` |
| 未登录 `/api/cart` | `401 Unauthorized` |
| 普通用户访问 `/api/admin/dashboard` | `403 Forbidden` |
| 登录用户提交订单 | 返回订单号 |
| 下单前后商品库存 | 减少下单数量 |

## 日志证据

```bash
docker compose -f docker-compose.microservices.yml logs --tail=120 order-service catalog-service
```

需要保留的关键日志：

```text
Feign call catalog-service productId=...
Internal order query catalog-service productId=...
Internal order deduct stock catalog-service productId=...
```

## 截图清单

截图建议统一放入 `docs/acceptance/evidence/`：

| 证据 | 建议文件名 |
| --- | --- |
| Nacos 5 个服务在线 | `evidence-nacos-services.png` |
| Docker Compose 服务运行状态 | `evidence-docker-microservices-ps.png` |
| Gateway `/api/products` 成功 | `evidence-gateway-products.png` |
| 401/403 鉴权结果 | `evidence-auth-401-403.png` |
| smoke test 全部 PASS | `evidence-smoke-test-pass.png` |
| 下单前后库存减少 | `evidence-stock-before-after.png` |
| order-service Feign 日志 | `evidence-feign-order-log.png` |
| catalog-service 内部接口日志 | `evidence-feign-catalog-log.png` |
| Nacos 服务列表网页截图 | `evidence-nacos-services-web.png` |
| 微服务前端首页 | `evidence-microservices-frontend-home-web.png` |
| 微服务商品列表 | `evidence-microservices-products-web.png` |
| 微服务登录页 | `evidence-microservices-login-web.png` |
| 微服务购物车 | `evidence-microservices-cart-web.png` |
| 微服务订单中心 | `evidence-microservices-orders-web.png` |
| 微服务后台登录 | `evidence-microservices-admin-login-web.png` |
| 微服务后台看板 | `evidence-microservices-admin-dashboard-web.png` |

说明：当前阶段服务按业务域拆分，但数据库仍共用 `ecommerce_minimal` 演示库。报告中应表述为“数据库后续可演进为独立库”，不要声称已完成数据库拆分。
