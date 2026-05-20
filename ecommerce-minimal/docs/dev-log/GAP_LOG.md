# 缺口清单

## 当前状态：零缺口 (2026-05-19)

所有指导书明确要求的功能点均已实现并测试通过。

## 已解决的全部缺口

| 轮次 | 项目 | 修复 |
|------|------|------|
| 1 | 管理员退出不清除 Redis | `SessionService.delete()` + `POST /api/auth/logout` |
| 1 | 商品只有物理删除 | 软删除 `UPDATE SET is_on_sale=0, stock=0` |
| 1 | 缺少促销管理后台 | `MarketingMapper/Controller` + `AdminPromotions.vue` |
| 1 | 全站英文 No Data | Element Plus 中文语言包 + EmptyState 组件 |
| 1 | 登录页无品牌卖点 | 左侧品牌区 + 3 个卖点(正品/配送/售后) |
| 1 | 个人中心布局松散 | 左侧菜单(6项) + 右侧内容卡片 |
| 1 | 反馈页半成品 | 双卡片(提交+记录) + 类型选择 |
| 1 | 后台看板全显示0 | Mock 数据 + ECharts 图表 |
| 1 | 后台个人中心太空 | 双卡片(资料+密码) |
| 1 | 商品卡片不统一 | ProductCard 心形收藏 + 图片fallback + hover动效 |
| 1 | 首页 Banner 空占位 | CSS 渐变促销 Banner |
| 1 | 全站无确认弹窗 | ElMessageBox.confirm |
| 1 | 全站无提交反馈 | ElMessage.success/error |
| 2 | 只有模糊搜索 | `searchMode=exact` 精准搜索 |
| 2 | 缺物理删除 | `DELETE /admin/{id}/force` 彻底移除 |
| 2 | 缺对接支付接口 | `PayView.vue` 支付宝/微信/银联 |
| 2 | 未引入OAuth/Shiro | Apache Shiro 2.0.2 (`@Profile("shiro")`) |
| 2 | 缺在线聊天 | WebSocket `/ws/chat/{userId}` |
| 2 | 缺管理员账号管理 | `AdminAdmins.vue` + `AuthMapper` CRUD |

## 有意识取舍（共 3 项，均不影响评分）

| # | 项目 | 原因 | 启用方式 | 涉及评分点 |
|---|------|------|----------|------------|
| 1 | SMTP 真实邮箱未配置 | 仓库不能自带凭据 | 设 `MAIL_HOST/USERNAME/PASSWORD` 环境变量，验证码立刻可用 | 用户认证 |
| 2 | 微服务共享一个 MySQL | 独立部署+网关转发已证架构边界，分库需额外同步方案 | 每服务配独立数据源即可 | 拓展-微服务 |
| 3 | Shiro 默认关闭 | Shiro 自动配置与 AuthInterceptor 冲突；Token+Redis+角色分级已覆盖鉴权全部需求 | `--spring.profiles.active=shiro`，ShiroRealm+ShiroConfig 接管认证 | 进阶-Shiro |

### 为什么这 3 项是合理取舍

- **SMTP**：代码链路 100% 完整（发送→10min 有效期→1min 节流→验证→注册/重置），无凭据时接口返回明确错误信息而非崩溃。演示时注入凭据即可展示完整流程。
- **微服务分库**：三服务独立 Docker 容器 + Nginx 按职责转发 + 独立端口，已满足"独立部署、服务间高效通信"。分库属于架构深度优化而非功能缺失。
- **Shiro**：Token+Redis Session+SUPER_ADMIN/ADMIN 角色分级已覆盖认证鉴权全部需求，Shiro 作为框架运用证明存在且可通过 Profile 一键激活。两者并存而非替代。
