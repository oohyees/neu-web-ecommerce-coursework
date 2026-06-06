# 电商平台大作业最佳实践 — 基于10份优秀代码的综合分析

> 分析日期：2026-06-06
> 数据来源：10位成绩优秀同学的完整工程源码
> 对照依据：《A0801051040-Web开发技术-实验指导书-2026》评分标准

---

## 一、评分表驱动的开发策略

指导书评分 = **基础20分 + 进阶20分 + 拓展10分 + 报告10分 = 60分（实验部分）**

按重要性排序，每个得分点都有对应的最佳实现范式：

### 1.1 基础分 (20分) — 必须全覆盖，零容错

| 得分点 | 分值 | 最佳实践 | 常见丢分 |
|--------|------|---------|---------|
| 客户端-商品 | 3分 | 分类树API + 搜索(精准/模糊双模式) + 排序(价格/销量/新品) + 收藏 + 评价(含图片上传) | 缺精准搜索、收藏/评价只做了前端没接后端 |
| 客户端-购物车 | 3分 | 加购选规格和数量、列表展示(图/名/价/数/小计)、勾选+全选反选、结算跳转 | 全选反选只在前端做假数据 |
| 客户端-订单 | 4分 | 确认页(地址+清单+总价) → 提交(订单号+支付方式) → 模拟支付 → 状态Tab(6种) → 取消/收货/退款/物流 | 缺退款流程、物流没有轨迹 |
| 后台-管理员 | 2分 | 管理员登录+权限验证+退出、SUPER_ADMIN/ADMIN双角色 | 只做了一种角色 |
| 后台-数据 | 2分 | 统计卡片(用户/订单/销售额/今日) + ECharts图表(趋势/排行/状态) | 图表只做了前端假数据 |
| 后台-用户 | 2分 | 分页列表+搜索(账号/手机/昵称)+启用禁用+详情 | 缺搜索或禁用功能 |
| 后台-商品 | 2分 | 分类CRUD+排序 + 商品CRUD+上下架+图片上传 + 评价管理 | 漏了分类排序或物理删除 |
| 后台-订单 | 2分 | 订单列表(筛选)+详情(商品/地址/支付/物流)+发货/取消/退款/状态修改+Excel导出 | 缺Excel导出 |

### 1.2 进阶分 (20分) — 决定排名

| 得分点 | 分值 | 最佳实践 |
|--------|------|---------|
| 前端技术 | 2分 | **Element Plus + Pinia**，缺一不可 |
| 后端技术 | 2分 | Spring Boot + MyBatis-Plus + **认证鉴权框架(OAuth或Shiro)** |
| 界面设计 | 4分 | 简洁美观+操作流畅+布局统一+**明确操作反馈**+无布局错乱 |
| 客户端进阶 | 4分 | 邮箱注册(验证码)+登录(记住密码)+找回密码+个人信息修改；首页轮播+分类导航+热门/新品/促销；收货地址CRUD+默认；规格选择+优惠券+秒杀/促销 |
| 后台进阶 | 4分 | 轮播/广告管理；公告/反馈管理+个人中心；数据统计导出+商品批量导入导出+权限分级 |
| 其他进阶 | 4分 | **响应式布局**(2分)+**图片上传**(1分)+**数据分页**(1分) |

### 1.3 拓展分 (10分) — 拉开差距

| 得分点 | 分值 | 最佳实践 |
|--------|------|---------|
| Redis缓存 | 3分 | `@Cacheable`商品详情+首页聚合、Session存Redis、验证码10min过期+1min节流 |
| 微服务 | 4分 | Nacos注册中心 + Spring Cloud Gateway + OpenFeign服务调用 + 独立数据库 |
| Docker容器化 | 3分 | docker-compose一键启动全套(MySQL+Redis+Nacos+服务+前端)、healthcheck依赖 |

---

## 二、后端技术栈最佳实践

### 2.1 技术选型黄金组合（经10份代码验证）

```
后端骨架: Spring Boot 3.3.x + Java 17 (不要用Java 11或23)
ORM:      MyBatis-Plus 3.5.x (90%同学的选择，JPA是少数)
认证:     JWT + Spring Security (标准) 或 JWT + Shiro (加分项)
服务发现: Nacos 2.x (真正的微服务标准)
服务通信: OpenFeign (不要手写RestTemplate路由)
网关:     Spring Cloud Gateway (不是手写Controller转发)
数据库:   MySQL 8.0 (微服务阶段可分库)
缓存:     Redis 7.x
```

### 2.2 传统Web技术证据 — 被最多人忽略的必得分

**10人中仅1人有实现！** 这是指导书明确要求的课程核心技术：

```java
// 必须实现的最小集合：
Servlet   → 至少1个HttpServlet子类，处理HTTP请求并返回HTML或JSON
JSP       → 至少1个.jsp页面，或用Controller渲染等同效果的HTML
Listener  → ServletContextListener（记录启动时间） + HttpSessionListener（统计在线人数）
Filter    → 至少1个Filter（可以是审计日志过滤器或鉴权过滤器）
JDBC      → 至少1处原生JDBC查询（不用MyBatis/ORM，直接Statement/PreparedStatement）
```

**最佳实践参考**：李旋同学的代码实现最为完整——`@WebFilter`鉴权过滤器 + `@WebListener`上下文/会话监听器 + JSP登录控制器 + JdbcDemoService原生JDBC。你们的项目也有完整实现（`LegacyStatusServlet`等）。

### 2.3 数据库设计最佳实践

**推荐方案**（commerce-web同学的做法）：

```
采用 Flyway 数据库迁移管理：
- V1__identity.sql       用户/管理员/角色
- V2__catalog.sql         商品/分类/SKU
- V3__cart.sql            购物车
- V4__order_payment.sql   订单/支付/物流
- V5__marketing.sql       优惠券/促销/秒杀
- V6__content.sql         轮播/公告/反馈/咨询
```

优点：版本化管理、可追溯、可回滚、Docker启动时自动执行。

**分库策略**（拓展阶段）：

```
ecommerce_user     → 用户、管理员、地址、会话
ecommerce_product  → 商品、分类、评价、收藏、轮播
ecommerce_order    → 订单、订单项、物流、购物车
ecommerce_marketing → 优惠券、促销、秒杀、规格
ecommerce_content  → 公告、反馈、咨询、活动通知
```

### 2.4 API设计规范

**统一响应格式**（全10个项目一致）：

```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... }
}
```

**分页格式**：

```json
{
  "success": true,
  "data": {
    "items": [...],
    "total": 100
  }
}
```

**RESTful风格命名**（张臻、李伟翔等做得最好）：

```
GET    /api/products              # 列表（公开）
GET    /api/products/{id}         # 详情（公开）
POST   /api/products/admin        # 新增（ADMIN）
PUT    /api/products/admin        # 编辑（ADMIN）
DELETE /api/products/admin/{id}   # 软删除（ADMIN）
DELETE /api/products/admin/{id}/force  # 物理删除（ADMIN）
```

### 2.5 认证鉴权最佳实践

**JWT Token流程**（标准做法）：

```
1. 登录 → 后端验证账号密码 → 生成JWT(含userId/role/exp) → 返回token
2. 前端存储token到localStorage → Axios拦截器自动加Authorization header
3. 后端Filter/Interceptor验证JWT → 解析用户信息 → 注入请求上下文
4. 退出 → 后端将token的jti加入Redis黑名单 → 网关拒绝黑名单token
```

**Shiro集成**（加分项，张臻、杨浩伟做了）：

```java
// Shiro Realm: 从数据库加载用户/角色/权限
// Shiro Config: 配置URL过滤器链
// @RequiresPermissions / @RequiresRoles 注解方法级鉴权
```

### 2.6 Redis缓存最佳实践

```java
// 商品详情缓存（访问最频繁）
@Cacheable(value = "productDetail", key = "#id")
public Product detail(Long id) { ... }

// 首页聚合数据缓存
@Cacheable(value = "home", key = "'index'")
public Map homeData() { ... }

// 写操作清除缓存
@CacheEvict(value = {"productDetail", "home"}, allEntries = true)
public void update(Product p) { ... }

// 验证码（10分钟有效，1分钟节流）
redisTemplate.opsForValue().set("verify:REGISTER:" + email, code, Duration.ofMinutes(10));
redisTemplate.opsForValue().set("verify:throttle:REGISTER:" + email, "1", Duration.ofMinutes(1));

// Session存储
redisTemplate.opsForValue().set("session:" + token, sessionInfo, Duration.ofHours(24));
```

---

## 三、前端技术栈最佳实践

### 3.1 推荐技术组合（张臻的项目为最佳范例）

```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.5.0",
    "@element-plus/icons-vue": "^2.3.0",
    "axios": "^1.6.0",
    "echarts": "^5.5.0"
  },
  "devDependencies": {
    "typescript": "^5.4.0",
    "vite": "^5.0.0",
    "tailwindcss": "^3.4.0",
    "@playwright/test": "^1.60.0"
  }
}
```

### 3.2 前端架构最佳实践

**项目结构**（张臻、commerce-web做法）：

```
frontend/
├── client/          # 用户端（独立Vue项目，端口5173）
│   └── src/
│       ├── router/     # 路由（含守卫）
│       ├── stores/     # Pinia状态（user、cart）
│       ├── api/        # Axios封装+接口定义
│       ├── views/      # 页面组件
│       └── components/ # 通用组件
└── admin/           # 管理后台（独立Vue项目，端口5174）
    └── src/
        ├── router/     # 路由（含admin权限守卫）
        ├── stores/     # admin状态
        ├── api/
        └── views/
```

**关键点**：
- 用户端和管理后台分开为两个独立Vue项目（7/10同学的做法）
- TypeScript > JavaScript（张臻、郭振顺、李伟翔、commerce-web都用TS）
- Pinia做状态管理（不用Vuex）
- 路由守卫实现认证鉴权
- Axios拦截器统一加token和错误处理

### 3.3 页面清单（对照评分表全覆盖）

**客户端页面**：

```
/                   首页（轮播+分类导航+热门+新品+促销+搜索）
/products           商品列表（分类筛选+关键词搜索+排序+分页）
/products/:id       商品详情（图片+规格+价格+库存+参数+详情+评价+收藏）
/login              登录（记住密码）
/register           注册（邮箱+验证码）
/forgot-password    找回密码
/cart               购物车（列表+勾选+改数量+全选+结算）
/checkout           订单确认（地址+清单+优惠券+总价）
/pay/:id            支付页（模拟支付）
/user/orders        我的订单（6状态Tab）
/user/orders/:id    订单详情（物流轨迹）
/user/profile       个人资料（头像/昵称/邮箱/密码修改）
/user/addresses     收货地址（CRUD+默认）
/user/favorites     我的收藏
/user/coupons       我的优惠券
/user/feedback      我的反馈
/user/consultations 我的咨询
/announcements      公告列表
```

**管理后台页面**：

```
/admin/login            管理员登录
/admin/dashboard         数据看板（统计卡片+ECharts图表）
/admin/products          商品管理（列表+搜索+新增/编辑Drawer+上下架+删除+导入导出）
/admin/categories        分类管理（树形+排序）
/admin/orders            订单管理（筛选+详情+发货+退款+状态修改+导出）
/admin/users             用户管理（搜索+启用/禁用+详情）
/admin/banners           轮播管理（CRUD+上传+排序）
/admin/announcements     公告管理（CRUD）
/admin/feedback           反馈管理（查看+回复+标记处理）
/admin/consultations      咨询管理（查看+回复）
/admin/admins             管理员管理（仅SUPER_ADMIN，CRUD+角色分配）
/admin/coupons            优惠券管理（CRUD）
/admin/promotions         促销管理（CRUD+秒杀）
/admin/profile            个人中心（修改密码）
```

### 3.4 响应式布局（2分）

必须有390px（手机）和768px（平板）两个断点的截图证据。后台移动端通常用抽屉式导航。

---

## 四、Docker部署最佳实践

### 4.1 张臻的最佳范例

```yaml
services:
  nacos:        # 先启动基础设施
    healthcheck: ...
  mysql:
    healthcheck: ...
  redis:
    healthcheck: ...
  auth-service: # 业务服务依赖基础设施
    depends_on:
      mysql: { condition: service_healthy }
      nacos: { condition: service_healthy }
  gateway:      # 网关最后启动
    depends_on:
      auth-service: { condition: service_started }
      product-service: { condition: service_started }
```

### 4.2 关键要点

- 必须对MySQL/Nacos/Redis做healthcheck
- 业务服务通过`depends_on`控制启动顺序
- 数据库初始化SQL放在`/docker-entrypoint-initdb.d/`
- 上传文件通过Docker volume持久化
- 前端Nginx做API反向代理到Gateway

---

## 五、测试最佳实践

### 5.1 后端测试（你们项目已经做得最好）

你们的`EcommerceScoringTests`有71个测试用例，对照评分表编写——这是10个项目中最完善的测试方案。

```java
// 测试命名规范（你的写法已经是范例）
@Test @Order(1)
@DisplayName("0.5分 商品分类浏览 - 获取分类列表")
void productCategories() { ... }
```

### 5.2 冒烟测试脚本

你项目的`acceptance_api_smoke.py`覆盖8组业务流程，这也是最佳实践。

### 5.3 前端测试（可补充）

```bash
# 张臻的做法
npm --prefix frontend/client run test:regression  # Playwright端到端

# commerce-web的做法
npm --prefix frontend/store-web run test          # Vitest单元测试
```

---

## 六、文档最佳实践

### 6.1 必要的文档清单

```
README.md              # 项目简介+快速启动+访问地址+默认账号
docs/architecture.md   # 架构图+技术栈+模块说明+认证流程
docs/api-reference.md  # 完整API列表（方法+路径+鉴权+说明）
docs/development.md    # 开发环境配置+命令+调试
docs/deployment.md     # Docker部署步骤+环境变量
docs/course/实验报告.md  # 按指导书章节组织的实验报告
```

### 6.2 验收清单（你项目已有的最佳实践）

`docs/course/acceptance/checklist.md` — 逐条对照指导书原文 + 评分表的覆盖矩阵。这是你应该在提交时附带的检查文档。

---

## 七、创新加分项（拉开差距的关键）

基于10份代码中出现的亮点，按性价比排序：

| 创新点 | 实现难度 | 加分潜力 | 参考同学 |
|--------|---------|---------|---------|
| **传统Web技术证据** | 低 | ⭐⭐⭐ 必得不丢 | 李旋 |
| **Shiro认证鉴权** | 低 | ⭐⭐ 指导书明确加分 | 张臻、杨浩伟 |
| **CI/CD (GitHub Actions)** | 中 | ⭐⭐ 自动构建+推送镜像 | 张臻 |
| **Flyway数据库迁移** | 低 | ⭐⭐ 专业规范 | commerce-web |
| **AI客服集成** | 中 | ⭐⭐⭐ 创新亮点 | 代宇霆(DeepSeek) |
| **RabbitMQ消息队列** | 高 | ⭐⭐⭐ 架构深度 | e_commerce、web-dev-exp |
| **WebSocket在线聊天** | 中 | ⭐⭐ 实时通信 | 杨浩伟、e_commerce |
| **EasyExcel导入导出** | 低 | ⭐⭐ 替代POI | 代宇霆 |
| **Playwright E2E测试** | 中 | ⭐⭐ 前端质量 | 张臻 |
| **Seata分布式事务** | 高 | ⭐⭐⭐ 最高难度 | web-dev-exp |
| **阿里云OSS** | 中 | ⭐ 生产级 | e_commerce |

---

## 八、提交清单（对照指导书）

### 8.1 工程压缩包

```bash
# 压缩包内容（参考你的create_submission_zip.sh）
# ✅ 包含：frontend/ backend/ docker/ docs/ scripts/ sql/ pom.xml README.md
# ❌ 排除：node_modules/ target/ dist/ uploads/ logs/ .git/ docs/archive/
# 大小限制：< 50M
```

### 8.2 实验报告

按指导书章节组织：
1. 实验目的
2. 实验内容
3. 实验环境
4. 实验过程与分析（每个模块+截图证据）
5. 实验创新点（微服务+缓存+Docker+特色功能）
6. 实验总结

### 8.3 截图证据清单

```
docs/course/acceptance/evidence/
├── 客户端/
│   ├── 首页.png
│   ├── 商品列表+搜索+排序.png
│   ├── 商品详情+评价.png
│   ├── 购物车+结算.png
│   ├── 订单确认+支付.png
│   ├── 我的订单+6状态Tab.png
│   ├── 收货地址管理.png
│   ├── 登录注册+找回密码.png
│   └── 响应式-390px.png / 响应式-768px.png
├── 管理后台/
│   ├── 登录.png
│   ├── 数据看板+图表.png
│   ├── 商品管理+导入导出.png
│   ├── 订单管理+发货+退款+导出.png
│   ├── 用户管理.png
│   ├── 轮播+公告+反馈管理.png
│   └── 响应式-移动端.png
├── Docker/
│   ├── docker-compose启动.png
│   └── 各容器运行状态.png
└── 微服务/
    ├── Nacos服务列表.png
    └── Feign调用日志.png
```

---

## 九、你的项目优势与待补项

### 当前优势（10人中第一梯队）

| 优势 | 说明 |
|------|------|
| 传统Web证据 | ✅ 完整Servlet/JSP/Listener/Filter/JDBC |
| 微服务架构 | ✅ Nacos + Gateway + Feign + 5服务独立数据库 |
| Redis缓存 | ✅ @Cacheable商品/首页 + Session + 验证码 |
| Docker | ✅ 双栈(单体+微服务) + healthcheck |
| 测试 | ✅ **71项测试 + 冒烟脚本（10人中最好）** |
| 文档 | ✅ API文档 + 架构 + 部署 + 验收清单 |
| Shiro | ✅ ShiroRealm + ShiroConfig（可选profile） |

### 待补充项

| 待补 | 优先级 | 建议 |
|------|--------|------|
| **前端重做** | 🔴 最高 | 参照张臻的方案：Vue3+TS+Element Plus+Pinia+Tailwind，前后台分离 |
| **响应式截图** | 🔴 高 | 390px和768px两个断点 |
| **Flyway迁移** | 🟡 中 | 将SQL改为Flyway版本化管理 |
| **CI/CD** | 🟡 中 | GitHub Actions自动构建 |
| **AI/创新功能** | 🟢 低 | 可选：DeepSeek客服、WebSocket聊天 |

---

## 十、总结：大作业满分公式

```
┌─────────────────────────────────────────────────────────┐
│  基础分 20  = 客户端10个API + 后台10个API (全做=满分)        │
│  进阶分 20  = ElementPlus+Pinia + Shiro + 界面 + 全部进阶功能  │
│  拓展分 10  = Redis缓存 + Nacos微服务 + Docker Compose       │
│  报告分 10  = 实验报告.docx + 截图证据                        │
│  ────────────────────────────────────────────               │
│  关键底线：传统Web技术证据 (Servlet+JSP+Listener+Filter+JDBC) │
│  关键差距：前端质量 + 微服务标准度 + 创新功能                   │
└─────────────────────────────────────────────────────────┘
```

**你的当前得分预估（非前端）**：基础20 + 进阶14(后端8+界面4+其他2) + 拓展10 = **44/50（仅后端+基础设施）**

**前端重做后目标**：基础20 + 进阶20 + 拓展10 = **50/50（满分）**
