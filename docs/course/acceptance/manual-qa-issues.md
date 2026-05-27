# 人工巡检问题记录

更新时间：2026-05-27。

| 编号 | 问题 | 影响 | 处理结果 |
| --- | --- | --- | --- |
| QA-001 | 商品列表页默认显示 0 件商品。Element Plus 数字输入框会把空价格筛选值等价为 `0`，前端首屏请求带上 `maxPrice=0` 时会过滤掉全部正常价格商品。 | 高。`/products` 是基础验收入口，空列表会直接影响商品浏览、搜索、排序演示。 | 已修复。`ProductList.vue` 只在最低价或最高价为正数时传递价格筛选参数；已重建前端容器并验证商品列表恢复为 40 件商品和分页。 |
| QA-002 | 验收证据 README 写“55 张页面截图”，实际 `docs/course/acceptance/evidence` 下为 53 张 PNG。 | 中。材料数量不一致会降低文档可信度。 | 已修复。`docs/course/acceptance/README.md` 已改为 53 张，并确认关键截图文件存在。 |
| QA-003 | 当前数据库含自动化冒烟和导入测试留下的 QA 用户、测试分类、导入商品、测试订单、测试地址；购物车里还出现过导入商品库存不足，影响购买闭环演示。 | 高。最终课堂演示或重新截图时会看到测试痕迹，也可能因库存不足中断下单流程。 | 已记录并补齐修复路径。新增 `scripts/reset_demo_database.sh`，与既有 PowerShell 脚本等价，可在 macOS/Linux 上按 `schema.sql` 和 `data.sql` 重置演示数据库并清理 Redis。为避免误删当前测试数据，本轮未直接执行重置。 |

## 本轮复验

| 命令/方式 | 结果 |
| --- | --- |
| 浏览器巡检用户端首页、商品列表、商品详情、购物车、结算、支付、订单 | 通过 |
| 浏览器巡检后台登录、看板、商品、订单、用户 | 通过 |
| 390px、768px 响应式巡检 | 通过 |
| `npm --prefix apps/web run build` | 通过，仅既有 Vite chunk-size 警告 |
| `./scripts/acceptance_check.sh` | 通过 |
| `python3 scripts/acceptance_api_smoke.py` | 8 组全部 PASS |
| `mvn test` | 71 tests, 0 failures, 0 errors |
| `./scripts/create_submission_zip.sh` | 通过，源码包约 4.4M |
