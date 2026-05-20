# Microservices Deployment

真实部署拆分：
- `gateway`：统一入口 `http://localhost:8088`
- `frontend`：通过网关访问各服务，入口 `http://localhost:18082`
- `auth-service`：认证、管理员、会话、权限，独立端口 `8081`
- `product-service`：商品、分类、评价、首页、营销、活动通知，独立端口 `8082`
- `order-service`：购物车、地址、订单、物流、客服咨询，独立端口 `8083`

启动：
```bash
docker compose up --build
```

最小验证：
```bash
# 微服务前端
curl http://localhost:18082

# 网关按职责转发
curl http://localhost:8088/api/home
curl "http://localhost:8088/api/products?page=1&size=5"
curl -X POST http://localhost:8088/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"123456\"}"

# 订单接口需要登录后携带用户 token
curl "http://localhost:8088/api/orders?userId=1" -H "Authorization: Bearer <user-token>"
```

实现方式：
- 三个服务由同一业务代码基线构建，但通过 `APP_SERVICE_SCOPE` 启动为不同职责实例
- 网关按路径转发到不同服务：`/api/auth` 到认证服务；`/api/products`、`/api/categories`、`/api/home`、`/api/marketing` 到商品服务；`/api/orders`、`/api/cart`、`/api/addresses` 到订单服务
- 当前版本共享 MySQL 与 Redis，服务部署独立、接口边界独立，适合课程项目阶段展示
