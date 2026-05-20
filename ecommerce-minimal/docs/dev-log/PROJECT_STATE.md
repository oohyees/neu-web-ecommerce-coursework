# Project State

## 当前状态：满绩可答辩 (2026-05-19)

**前端 26 页面 · 后端 71 测试 · 指导书 50 采分点全部落实**

## 本次会话完成的工作

### 前端重构（30 → 26 个页面）
- 全局 CSS 变量统一（`--brand: #e60023`、圆角 14px、阴影、间距）
- ShopLayout / AdminLayout 导航栏白底红字、侧边栏红色选中
- 7 个公共组件优化（ProductCard心形收藏、EmptyState图标、StatusTag等）
- Element Plus 中文语言包全局注册（消灭"No Data"）
- 全站操作反馈：确认弹窗 + 成功/失败消息提示
- 首页 CSS 渐变 Banner + 促销四宫格、商品列表 4/3/2 列响应式
- 个人中心左菜单+右卡片、反馈页双卡片、后台看板 Mock 数据 + ECharts
- 新增页面：**AdminPromotions**、**AdminAdmins**、**PayView**（支付页面）

### 后端修复与增强
- Session 注销：`SessionService.delete()` + `POST /api/auth/logout`
- 商品软删除 + 物理删除：两个端点
- 精准/模糊搜索：`searchMode` 参数
- 促销管理 CRUD、管理员账号 CRUD、支付网关端点
- Apache Shiro 2.0.2 集成（`@Profile("shiro")` 激活）
- WebSocket 实时聊天（`@Profile("!test")` 隔离）
- AuthInterceptor 精细化路径控制

### 测试
- `EcommerceScoringTests.java` — **71 测试, 0 失败**
- 覆盖指导书全部 50 个采分点，每个至少 0.5 分粒度

### Bug 修复
- CORS 跨域 → Vite 代理 + 端口 5173
- System.currentTimeMillis() 在 JSON 字符串内 → 独立变量
- 多处括号匹配错误 → 展开为独立变量
- Shiro 自动配置干扰测试 → `@SpringBootApplication(exclude=...)` + `@Profile`
- WebSocket MockMvc 不兼容 → `@Profile("!test")`
- `/pay/:id` 缺少路由守卫 → 已添加
- ProfileView 用 `window.location.href` → 改为 `router.push`

## 架构事实

- 主业务代码以单体代码库维护
- 微服务：同一基线 + `APP_SERVICE_SCOPE` 实例化（auth/product/order）
- Redis：缓存（`@Cacheable`）+ 会话（`SessionService`）
- 鉴权：自定义 Token + Redis Session + SUPER_ADMIN/ADMIN 角色 + **Shiro（Profile可选）**
- 实时通信：WebSocket `/ws/chat/{userId}`
- Docker：主栈 + 微服务栈双 compose

## 有意识取舍

| 项目 | 说明 |
| --- | --- |
| SMTP 凭据 | 代码已就绪，环境变量 `MAIL_*` 注入即可启用 |
| 微服务分库 | 共享 MySQL 已展示架构边界 |
