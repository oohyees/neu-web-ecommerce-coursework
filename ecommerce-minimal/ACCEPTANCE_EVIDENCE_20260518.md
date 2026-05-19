# 验收证据索引（2026-05-18）

## 浏览器截图

- 用户端：
  - `evidence-user-home.png`
  - `evidence-user-products.png`
  - `evidence-user-cart.png`
  - `evidence-user-checkout.png`
  - `evidence-user-orders.png`
  - `evidence-user-consultations.png`
- 管理端：
  - `evidence-admin-dashboard.png`
  - `evidence-admin-products.png`
  - `evidence-admin-orders.png`
  - `evidence-admin-consultations.png`
- 响应式：
  - `evidence-responsive-390.png`
  - `evidence-responsive-768.png`
- Docker 前端：
  - `evidence-docker-main-frontend.png`
  - `evidence-docker-microservices-frontend.png`

## 本轮已复测

- 前端构建：`npm run build`
- 后端测试：`mvn test`
- 本地接口烟测：
  - 用户登录
  - 管理员登录
  - 首页
  - 商品
  - 购物车
  - 地址
  - 订单
  - 客服咨询
  - 后台看板
  - 用户管理
  - 商品管理
  - 订单管理
  - 活动通知
  - 客服咨询管理
- 根目录 Docker 主栈：
  - `18080` 后端接口可访问
  - `18081` 前端可访问
- 微服务栈：
  - `8088` 网关登录 / 商品 / 首页接口可访问
  - `18082` 前端可访问

## 当前口径

- SMTP：真实发信代码已实现，当前演示环境未注入可用邮箱账号。
- 客服：当前实现为留言 / 回复闭环，不是实时聊天。
- 微服务：当前为三实例拆分，仍共享一个 MySQL 库。
