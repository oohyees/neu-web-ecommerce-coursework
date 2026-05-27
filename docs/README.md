# 项目文档

> 5 分钟了解全貌 → [architecture.md](architecture.md)｜卡住了 → [development.md](development.md)｜查接口 → [api-reference.md](api-reference.md)

## 我想…

| 场景 | 路径 |
|------|------|
| **第一次接手项目** | [architecture.md](architecture.md) → [development.md](development.md) → 跑起来 |
| **启动/运行项目** | [development.md](development.md) §常用命令 → [deployment.md](deployment.md) |
| **理解代码结构** | [architecture.md](architecture.md) §二(目录) → §五(单体) → §六(微服务) |
| **理解认证鉴权** | [architecture.md](architecture.md) §五-认证流程 → [api-reference.md](api-reference.md) §鉴权说明 |
| **理解数据库表结构** | [architecture.md](architecture.md) §四 |
| **理解下单流程** | [architecture.md](architecture.md) §五-下单事务流程 |
| **查某个 API** | [api-reference.md](api-reference.md) → 按模块查找 |
| **添加新功能** | [architecture.md](architecture.md) 找对应模块 → [api-reference.md](api-reference.md) 找已有端点 → [development.md](development.md) §配置 |
| **排查启动失败** | [deployment.md](deployment.md) §健康检查 → [development.md](development.md) §已知限制 |
| **排查 401/403** | [architecture.md](architecture.md) §五-认证流程 → [development.md](development.md) §认证机制 |
| **准备答辩/验收** | [course/acceptance/](course/acceptance/) → [course/实验报告.md](course/实验报告.md) |
| **找历史记录** | [archive/](archive/) — 已过期，仅供参考 |

## 快速启动

```bash
# 单体模式（最常用）
docker compose up -d --build
# 前端 http://localhost:18081  |  API http://localhost:18080/api

# 微服务模式
./scripts/build_microservices.sh
docker compose -f docker-compose.microservices.yml up -d --build
```

默认账户：`alice / 123456`（用户）、`admin / admin123`（超管）

## 文档地图

```
docs/
│
├── README.md                ← 你在这
├── architecture.md          — 架构总览（分层、数据库、认证、微服务拓扑）
├── development.md           — 开发手册（环境、命令、配置、已知限制）
├── api-reference.md         — API 端点清单（按模块、含鉴权要求）
├── deployment.md            — 部署说明（Docker Compose、端口、环境变量）
│
├── course/                  — 课程交付物（只读）
│   ├── 实验指导书.md
│   ├── 实验报告.md
│   └── acceptance/          — 评分清单 + 55 张截图证据
│
└── archive/                 — 历史文档（不保证与代码一致）
```

## 项目概览（30 秒版）

- **后端**：Spring Boot 3.2.4 单体（`apps/api`） + Spring Cloud 微服务集群（`services/*`）
- **前端**：Vue 3 + Vite + Element Plus + Pinia（`apps/web`）
- **数据库**：MySQL 8.4，21 张表，utf8mb4
- **缓存/会话**：Redis 7，token 存 Redis，12h TTL
- **部署**：Docker Compose，单体和微服务两套配置
- **测试**：71 个集成测试，0 失败，覆盖全部评分点
