# 演示脚本（2026-05-19 最终版）

## 客户端演示（~6 分钟）

### 1. 首页（30 秒）
- `http://localhost:5173/`
- 三栏布局（左侧分类/中间轮播Banner/右侧用户面板）
- 促销四宫格、热门商品/新品推荐 4 列卡片
- 搜索框胶囊样式 + 热门搜索词

### 2. 商品浏览与搜索（1 分钟）
- `/products`：30 件商品、9 分类胶囊
- **精准搜索**：`searchMode=exact` 精确匹配 vs `searchMode=fuzzy` 模糊
- 排序切换：综合→新品→销量→价格升序→价格降序
- `/products/1`：秒杀价展示、规格选择（颜色/尺寸）、数量步进器
- 收藏（♡⇄♥）+ 加购物车 + 立即购买 + 正品保障

### 3. 购物车 → 支付（1.5 分钟）
- `/cart`：全选/反选、步进器改数量、删除确认弹窗
- `/checkout`：地址卡片（默认标签）、商品清单、优惠券、支付方式
- **对接支付**：`/pay/:id` → 支付宝/微信/银联三选一 → 扫码模拟 → 确认支付

### 4. 我的订单（1 分钟）
- `/orders`：6 状态 Tab（全部/待支付/待发货/待收货/已完成/已取消）
- 立即支付→支付页面、取消（确认弹窗）、确认收货、申请退款
- 订单详情：商品+地址+物流轨迹

### 5. 个人中心与客服（1 分钟）
- `/user/profile`：左侧菜单 6 项、资料编辑、头像上传、优惠券卡片
- `/feedback`：双卡片（提交反馈+类型选择+反馈记录）
- `/consultations`：**REST 留言 + WebSocket 实时在线聊天**

### 6. 用户认证（1 分钟）
- `/register`：邮箱验证码注册（10 分钟有效期 + 1 分钟节流）
- `/login`：品牌卖点（正品保障/极速配送/售后无忧）+ 记住密码
- `/forgot-password`：验证码→重置密码

## 管理后台演示（~3 分钟）

### 7. 登录与看板（30 秒）
- `/admin/login`：深色主题
- `/admin/dashboard`：128 用户/356 订单/89240 销售额、ECharts 三图、待处理事项

### 8. 商品与促销管理（1 分钟）
- `/admin/products`：搜索/筛选/分页、新增/编辑 Drawer、图片上传、**软删除+物理删除**、CSV 导入/Excel 导出
- `/admin/categories`：分类 CRUD + 父分类
- `/admin/promotions`：**新增秒杀/直降促销**（设置商品/价格/时间/库存）
- `/admin/reviews`：查看/删除违规评价

### 9. 订单与用户管理（1 分钟）
- `/admin/orders`：状态筛选、详情、发货、退款处理、改状态、Excel 导出
- `/admin/users`：搜索、启用/禁用确认弹窗、详情
- `/admin/admins`：**管理员账号 CRUD**（用户名/密码/角色 ADMIN/SUPER_ADMIN）

### 10. 系统功能（30 秒）
- `/admin/banners`：轮播图 CRUD（上传/链接/排序/搜索）
- `/admin/announcements`：公告 CRUD
- `/admin/activity-notices`：活动通知 CRUD
- `/admin/feedback`：查看反馈→回复→标记已处理
- `/admin/profile`：双卡片（资料+密码）
- **退出登录**→Redis Session 销毁→旧 token 立即 401

## 拓展演示（~1 分钟）

### 11. 技术架构
- `redis-cli -p 6380 keys "session:*"` → Redis Session
- `redis-cli -p 6380 keys "home*"` → Redis 缓存
- `docker compose ps` → 主栈（MySQL+Redis+后端+前端）
- `cd microservices && docker compose ps` → 微服务三服务+网关
- `curl http://localhost:8088/api/products` → 微服务网关正常

### 12. Shiro 框架（可选激活）
- `mvn spring-boot:run -Dspring-boot.run.profiles=shiro` → Shiro 接管认证鉴权
