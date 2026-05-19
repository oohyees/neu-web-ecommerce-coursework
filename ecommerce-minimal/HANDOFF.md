# 交接说明

## 先读这些文件

1. `PROJECT_STATE.md`
2. `GAP_LOG.md`
3. `ACCEPTANCE_CHECKLIST_2026.md`
4. `ACCEPTANCE_REPORT.md`
5. `ISSUE_LOG.md`

## 当前真实进度

- 项目主功能已基本具备，已经不是“先补基础功能”的阶段
- 2026-05-18 已新增并验证：
  - 客服咨询
  - 活动通知
  - `SUPER_ADMIN / ADMIN` 权限分级
  - 真实 SMTP 验证码代码链路
  - 基于主业务代码的三实例微服务部署方式
- 当前最需要做的不是继续凭空扩功能，而是：
  1. 最终交付材料
  2. 如需更稳，再补少量专项复验

## 本轮实际做过的事

- 修复旧库与新代码不匹配的问题
- 生成数据库备份：
  - `ecommerce_minimal-before-migration-20260518-183109.sql`
- 新增数据库迁移：
  - `backend/src/main/resources/migration_20260518_complete.sql`
  - `backend/src/main/resources/migration_20260518_seed.sql`
- 重新验证：
  - `npm run build`
  - `mvn test`
  - `mvn -DskipTests package`
  - 多组接口
  - 完整订单闭环
  - 浏览器关键页
  - 根目录 Docker 主栈
  - 微服务栈

## 已确认能跑通的接口链路

- 用户侧：
  - 登录
  - 首页
  - 商品
  - 购物车
  - 地址
  - 订单
  - 反馈
  - 收藏
  - 活动通知
  - 客服咨询
- 管理侧：
  - 管理员登录
  - 用户管理
  - 活动通知管理
  - 客服咨询管理
  - 营销相关接口

## 目前不要误判的点

- `README.md`、旧版 `ACCEPTANCE_REPORT.md`、旧版 `ISSUE_LOG.md` 曾经写了不少“已完成验收”的历史结论，已按 2026-05-18 真实状态收敛，但仍应以这次交接文件为准
- 真实 SMTP **代码已接入**，但**没有可用邮箱配置就无法真实发信**
- 微服务目前是**真实部署拆分**，但仍是**共享库**
- 当前页面截图与 Docker 复验已经补齐，详见 `ACCEPTANCE_EVIDENCE_20260518.md`

## 接手后的最短路径

1. 先看 `ACCEPTANCE_EVIDENCE_20260518.md`
2. 决定 SMTP 是否注入真实账号
3. 如需更稳，再补轮播、导入导出、上传专项复验
4. 完成最终实验报告与压缩包

## 当前不要重复做的事

- 不要再重做：
  - 客服咨询
  - 活动通知
  - 管理员角色分级
  - 数据库补迁移
  - 微服务部署重构
- 不要把“文档里写过”当“当前机器已验证”
- 后续一律以当前机器上的可运行证据为准
