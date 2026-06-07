# 人工巡检问题记录

更新时间：2026-06-07。

| 编号 | 问题 | 影响 | 处理结果 |
| --- | --- | --- | --- |
| QA-001 | 商品列表页默认显示 0 件商品。Element Plus 数字输入框会把空价格筛选值等价为 `0`，前端首屏请求带上 `maxPrice=0` 时会过滤掉全部正常价格商品。 | 高。`/products` 是基础验收入口，空列表会直接影响商品浏览、搜索、排序演示。 | 已修复。`ProductList.vue` 只在最低价或最高价为正数时传递价格筛选参数；已重建前端容器并验证商品列表恢复为 40 件商品和分页。 |
| QA-002 | 验收证据 README 写“55 张页面截图”，实际 `docs/course/acceptance/evidence` 下为 53 张 PNG。 | 中。材料数量不一致会降低文档可信度。 | 已修复。`docs/course/acceptance/README.md` 已改为 53 张，并确认关键截图文件存在。 |
| QA-003 | 当前数据库含自动化冒烟和导入测试留下的 QA 用户、测试分类、导入商品、测试订单、测试地址；购物车里还出现过导入商品库存不足，影响购买闭环演示。 | 高。最终课堂演示或重新截图时会看到测试痕迹，也可能因库存不足中断下单流程。 | 已记录并补齐修复路径。新增 `scripts/reset_demo_data.sh`，与既有 PowerShell 脚本等价，可在 macOS/Linux 上按 `schema.sql` 和 `data.sql` 重置演示数据库并清理 Redis。为避免误删当前测试数据，本轮未直接执行重置。 |
| QA-004 | 首次执行 `scripts/reset_demo_data.sh` 失败。脚本用 MySQL `SOURCE` 命令通过批处理导入绝对路径 SQL，在当前 MySQL 客户端环境下解析为语法错误。 | 高。最终演示前如果不能一键恢复干净种子库，会继续残留 QA 数据，影响页面观感和购买链路稳定性。 | 已修复。脚本改为先导入 `schema.sql`，再对 `ecommerce_minimal` 导入 `data.sql`；本轮已实际执行成功，并用 Docker Redis 容器补充执行 `FLUSHDB` 清理缓存。 |
| QA-005 | 商品图片曾不显示。早期微服务运行库的商品 `image_url` 使用 `/catalog/...` 本地路径，但前端 `public/catalog` 目录曾被迁移删除，Nginx 对图片请求回退到 `index.html`；少量导入测试商品 `imageUrl` 为空，会被浏览器解析成 `/`。 | 高。首页、搜索、详情、购物车和后台预览都会出现坏图，直接影响课堂演示观感。 | 已修复。恢复本地 catalog 图片资源并新增 `/catalog/placeholder.svg` 作为空图兜底；当前商品主种子已切换为 DummyJSON 远程商品图，最终演示前重置数据库后应复验远程图和占位图两类路径。 |

## 本轮复验

| 命令/方式 | 结果 |
| --- | --- |
| 浏览器巡检用户端首页、商品列表、商品详情、购物车、结算、支付、订单 | 通过 |
| 浏览器巡检后台登录、看板、商品、订单、用户 | 通过 |
| 390px、768px 响应式巡检 | 通过 |
| `npm --prefix frontend/shop-web run build` | 通过，仅既有 Vite chunk-size 警告 |
| `./scripts/acceptance_check.sh` | 通过 |
| `python3 scripts/acceptance_api_smoke.py` | 8 组全部 PASS |
| `mvn test` | 71 tests, 0 failures, 0 errors |
| `./scripts/create_submission_zip.sh` | 通过，源码包约 4.4M |
| 浏览器首页图片加载检查 | 通过；14 张图片全部加载，坏图 0 |
| `curl -I http://127.0.0.1:18095/catalog/B076LRJ528.webp` | 通过，返回 `Content-Type: image/webp` |
| `curl -I http://127.0.0.1:18095/catalog/placeholder.svg` | 通过，返回 `Content-Type: image/svg+xml` |

## 重置后浏览器巡检

更新时间：2026-05-27。

| 巡检项 | 结果 |
| --- | --- |
| 执行 `./scripts/reset_demo_data.sh` | 首次失败并修复脚本；第二次成功恢复种子数据 |
| 清理 Redis | 本机无 `redis-cli`，已用 `docker compose exec -T redis redis-cli FLUSHDB` 成功清理 |
| 公共页面：首页、商品列表、商品详情、登录、注册、找回密码 | 通过；商品列表恢复为 30 件种子商品 |
| 用户端：登录、个人资料、地址、收藏、优惠券、账户安全、反馈、咨询 | 通过 |
| 购买闭环：加购、购物车、结算、提交订单、支付页、模拟支付、订单列表 | 通过 |
| 管理后台：登录、看板、商品、分类、评价、订单、咨询、反馈、轮播、促销、公告、活动通知、用户、管理员、个人资料 | 通过 |
| 响应式：390px、768px 首页、商品列表、后台移动菜单抽屉 | 通过 |

备注：重置后的浏览器购买链路巡检会重新创建 1 笔订单并扣减库存；最终正式演示前应再次执行重置脚本。
