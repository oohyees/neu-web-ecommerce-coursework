# 评分点代码证据映射

| 评分点 | 验收入口 | 关键代码 |
| --- | --- | --- |
| 商品浏览/详情/搜索/排序/分页 | `/products`, `/api/products` | `ProductController`, `ProductMapper.xml`, `ProductList.vue` |
| 购物车规格/数量/删除/结算 | `/cart`, `/api/cart` | `CartController`, `CartMapper.xml`, `CartView.vue` |
| 订单确认/支付/物流/退款 | `/checkout`, `/orders`, `/api/orders` | `OrderController`, `OrderService`, `OrderMapper.xml` |
| 管理员与权限分级 | `/admin`, `/api/auth/admin/**` | `AuthInterceptor`, `AuthController`, `AuthService` |
| 用户数据隔离 | 用户侧全部接口 | `CurrentSession`, `AddressController`, `FavoriteController`, `OrderController` |
| 密码安全 | 登录/注册/改密 | `AuthService` BCrypt 兼容与迁移 |
| 上传安全 | `/api/files/upload` | `FileController` 类型、大小、文件名校验 |
| Redis | 登录会话、商品缓存 | `SessionService`, `CacheConfig`, `@Cacheable` |
| Servlet | `/legacy/servlet/status` | `LegacyStatusServlet` |
| JSP | `/legacy/status` | `legacy-status.jsp`, `LegacyStatusController` |
| Listener | 启动时间、在线人数 | `LegacyAppListener` |
| Filter | 最近访问审计 | `LegacyAuditFilter` |
| JDBC | 统计数据查询 | `LegacyJdbcDao` |
| Docker 单体部署 | `docker compose up -d --build` | `docker-compose.yml` |
| 微服务拆分 | `docker compose -f docker-compose.microservices.yml up -d --build` | `services/gateway`, `services/*-service`, `libs/common` |

## 推荐演示顺序

1. 运行单体 Docker，打开首页、商品详情、购物车、下单、我的订单。
2. 用普通用户访问后台接口，展示 403；用管理员登录后访问成功。
3. 打开 `/legacy/status` 和 `/legacy/servlet/status`，展示传统 Web 技术。
4. 运行微服务 compose，访问网关 `/api/products`、`/api/auth/login`。
5. 使用 `scripts/create_submission_zip.sh` 生成交付包并展示体积。
