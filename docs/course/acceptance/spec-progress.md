# SPEC.md 阶段性执行清单

更新时间：2026-06-07

本清单用于把 `SPEC.md` 的改进方向拆成可执行状态。原则是优先降低现场逐项给分风险，不做高风险重构。

当前结论：核心页面、图片加载、前台购买链路、后台核心管理页、390px/768px 响应式和 legacy 传统 Web 证据已完成一轮人工巡检。最新逐项结论以 [checklist.md](checklist.md) 为准，问题和复验记录以 [manual-qa-issues.md](manual-qa-issues.md) 为准。

## 已确认具备闭环的 P0 项

| 项目 | 当前状态 | 主要入口/证据 |
| --- | --- | --- |
| 管理员角色与权限分配 | 已具备 SUPER_ADMIN/ADMIN 分级；后台菜单按角色显示，后端超级管理员接口返回 403 限制普通管理员 | `/admin/admins`，`/api/auth/admin/admins`，`AuthInterceptor`，`AuthGatewayFilter` |
| 商品规格选择闭环 | 商品详情选择规格，购物车、结算、用户订单、后台订单详情均显示 `specText` | `/products/:id`，`/cart`，`/checkout`，`/orders`，`/admin/orders` |
| 优惠券结算金额 | 用户可领取优惠券，结算页选择后实时显示优惠金额，后端下单校验并写入 `discount_amount` | 首页优惠券区，`/checkout`，`OrderService`，后台订单详情 |
| 分类排序 | 后台分类表单可新增/编辑 `sortOrder`，列表显示排序值 | `/admin/categories` |
| 商品/订单导入导出 | 商品页有导入/导出按钮，订单页有导出按钮；已新增小体量 CSV 样例 | `/admin/products`，`/admin/orders`，`sample-products-import.csv` |
| Redis 可见证据 | Session key、验证码、缓存均使用 Redis；重置脚本会清理 Redis | `SessionService`，`CacheConfig`，`scripts/reset_demo_data.sh` |
| 微服务证据 | Gateway/Nacos/OpenFeign/库存扣减由微服务烟测覆盖 | `docker/docker-compose.yml`，`scripts/microservices_smoke_test.sh` |
| Docker 兜底 | 已新增不重新构建的演示启动与服务检查脚本 | `scripts/demo_start_monolith.sh`，`scripts/demo_start_microservices.sh`，`scripts/demo_check_services.sh` |

## 本轮新增交付

| 文件 | 用途 |
| --- | --- |
| `scripts/demo_start_monolith.sh` | 用现有镜像启动单体演示栈并检查前端、后端、legacy 页面 |
| `scripts/demo_start_microservices.sh` | 用现有镜像启动微服务演示栈并检查前端、Gateway |
| `scripts/demo_check_services.sh` | 统一检查单体、微服务、Gateway，并输出 Redis 证据命令 |
| `scripts/final_precheck.sh` | 按最终验收顺序执行构建、测试、烟测、微服务烟测，最后重置干净演示数据 |
| `docs/course/acceptance/sample-products-import.csv` | 后台商品导入现场演示样例，只有 2 条数据 |

## 最终演示前复检项目

| 项目 | 当前状态 | 复检动作 |
| --- | --- | --- |
| 促销/秒杀库存闭环 | 功能与页面入口已覆盖 | 从首页促销商品进入详情、下单，再用后台或数据库确认库存变化 |
| 评价带图闭环 | 评价入口和后台评价管理已覆盖 | 用已完成订单提交带图评价，检查商品详情和后台评价管理 |
| 图片上传入口 | 商品图资源和空图兜底已修复，首页图片复验通过 | 至少验证后台商品图、轮播图、评价图中的两类上传入口 |
| 首页热门搜索词/二级分类 | 首页、搜索、商品详情已完成一轮巡检 | 最终截图前确认首页首屏入口、分类和搜索词显示正常 |
| 前台响应式 | 390px、768px 已完成一轮巡检 | 最终截图前复检首页、商品详情、购物车、结算、订单 |
| 客服/公告/反馈闭环 | 用户端和后台系统管理页已覆盖 | 按用户提交、后台处理、用户查看结果的顺序各走一遍 |
| Legacy 证据 | `/legacy/status` 和 legacy 前端已验证可访问 | 打开 `http://localhost:18080/legacy/status`，确认 Servlet/JSP/Listener/Filter/JDBC 信息可见 |

## 建议执行顺序

1. 先运行 `scripts/demo_start_microservices.sh`，确认默认微服务主展示路径可访问。
2. 执行 `./scripts/acceptance_check.sh` 和 `python3 scripts/acceptance_api_smoke.py`。
3. 运行 `scripts/demo_start_microservices.sh` 和 `scripts/microservices_smoke_test.sh`，保留 Nacos/Gateway/Feign 证据。
4. 运行 `scripts/reset_demo_data.sh both`，再进行截图或课堂演示。
5. 最终交付前运行 `scripts/final_precheck.sh`。
