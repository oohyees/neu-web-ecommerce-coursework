# Frontend Link Audit

- Shop: http://localhost:18095
- Admin: http://localhost:18082
- Generated: 2026-06-07T05:01:15.835Z

## Route Results

| Status | Kind | Route | Final URL | Text Len | Screenshot | Notes |
| --- | --- | --- | --- | ---: | --- | --- |
| PASS | shop-public | `/` | http://localhost:18095/ | 3371 | [shop-public-root.png](./shop-public-root.png) |  |
| PASS | shop-public | `/login` | http://localhost:18095/login | 45 | [shop-public-login.png](./shop-public-login.png) |  |
| PASS | shop-public | `/register` | http://localhost:18095/register | 45 | [shop-public-register.png](./shop-public-register.png) |  |
| PASS | shop-public | `/forgot-password` | http://localhost:18095/forgot-password | 46 | [shop-public-forgot-password.png](./shop-public-forgot-password.png) |  |
| PASS | shop-public | `/search?keyword=牛肉` | http://localhost:18095/search?keyword=%E7%89%9B%E8%82%89 | 113 | [shop-public-search-keyword.png](./shop-public-search-keyword.png) |  |
| PASS | shop-public | `/category/1` | http://localhost:18095/category/1 | 855 | [shop-public-category-1.png](./shop-public-category-1.png) |  |
| PASS | shop-public | `/product/1` | http://localhost:18095/product/1 | 57 | [shop-public-product-1.png](./shop-public-product-1.png) |  |
| PASS | shop-public | `/seckill` | http://localhost:18095/seckill | 525 | [shop-public-seckill.png](./shop-public-seckill.png) |  |
| PASS | shop-public | `/notices` | http://localhost:18095/notices | 159 | [shop-public-notices.png](./shop-public-notices.png) |  |
| PASS | shop-public | `/activities` | http://localhost:18095/notices | 159 | [shop-public-activities.png](./shop-public-activities.png) |  |
| PASS | shop-auth | `/cart` | http://localhost:18095/cart | 81 | [shop-auth-cart.png](./shop-auth-cart.png) | http: GET http://localhost:18095/api/home net::ERR_ABORTED; GET http://localhost:18095/api/cart net::ERR_ABORTED; GET http://localhost:18095/api/favorites net::ERR_ABORTED |
| PASS | shop-auth | `/checkout` | http://localhost:18095/checkout | 94 | [shop-auth-checkout.png](./shop-auth-checkout.png) |  |
| PASS | shop-auth | `/payment?orderNo=DEMO&id=1` | http://localhost:18095/payment?orderNo=DEMO&id=1 | 124 | [shop-auth-payment-orderNo-DEMO-id-1.png](./shop-auth-payment-orderNo-DEMO-id-1.png) |  |
| PASS | shop-auth | `/orders` | http://localhost:18095/orders | 673 | [shop-auth-orders.png](./shop-auth-orders.png) |  |
| PASS | shop-auth | `/orders/1` | http://localhost:18095/orders/1 | 341 | [shop-auth-orders-1.png](./shop-auth-orders-1.png) |  |
| PASS | shop-auth | `/profile` | http://localhost:18095/profile | 138 | [shop-auth-profile.png](./shop-auth-profile.png) |  |
| PASS | shop-auth | `/address` | http://localhost:18095/address | 174 | [shop-auth-address.png](./shop-auth-address.png) |  |
| PASS | shop-auth | `/favorites` | http://localhost:18095/favorites | 148 | [shop-auth-favorites.png](./shop-auth-favorites.png) |  |
| PASS | shop-auth | `/coupons` | http://localhost:18095/coupons | 220 | [shop-auth-coupons.png](./shop-auth-coupons.png) |  |
| PASS | shop-auth | `/feedback` | http://localhost:18095/feedback | 267 | [shop-auth-feedback.png](./shop-auth-feedback.png) |  |
| PASS | shop-auth | `/service` | http://localhost:18095/service | 207 | [shop-auth-service.png](./shop-auth-service.png) |  |
| PASS | shop-suspect | `/products` | http://localhost:18095/products | 757 | [shop-suspect-products.png](./shop-suspect-products.png) |  |
| PASS | shop-suspect | `/products?sort=sales_desc` | http://localhost:18095/products?sort=sales_desc | 689 | [shop-suspect-products-sort-sales-desc.png](./shop-suspect-products-sort-sales-desc.png) |  |
| PASS | shop-suspect | `/products?sort=newest` | http://localhost:18095/products?sort=newest | 761 | [shop-suspect-products-sort-newest.png](./shop-suspect-products-sort-newest.png) |  |
| PASS | admin | `/` | http://localhost:18082/dashboard | 224 | [admin-root.png](./admin-root.png) | http: GET http://localhost:18082/api/admin/dashboard net::ERR_ABORTED |
| PASS | admin | `/dashboard` | http://localhost:18082/dashboard | 224 | [admin-dashboard.png](./admin-dashboard.png) |  |
| PASS | admin | `/users` | http://localhost:18082/users | 390 | [admin-users.png](./admin-users.png) |  |
| PASS | admin | `/categories` | http://localhost:18082/categories | 660 | [admin-categories.png](./admin-categories.png) |  |
| PASS | admin | `/products` | http://localhost:18082/products | 908 | [admin-products.png](./admin-products.png) |  |
| PASS | admin | `/promotions` | http://localhost:18082/promotions | 496 | [admin-promotions.png](./admin-promotions.png) |  |
| PASS | admin | `/reviews` | http://localhost:18082/reviews | 751 | [admin-reviews.png](./admin-reviews.png) |  |
| PASS | admin | `/orders` | http://localhost:18082/orders | 660 | [admin-orders.png](./admin-orders.png) |  |
| PASS | admin | `/banners` | http://localhost:18082/banners | 367 | [admin-banners.png](./admin-banners.png) |  |
| PASS | admin | `/notices` | http://localhost:18082/notices | 234 | [admin-notices.png](./admin-notices.png) |  |
| PASS | admin | `/feedbacks` | http://localhost:18082/feedbacks | 339 | [admin-feedbacks.png](./admin-feedbacks.png) |  |
| PASS | admin | `/cs` | http://localhost:18082/cs | 241 | [admin-cs.png](./admin-cs.png) |  |
| PASS | admin | `/permission-manage` | http://localhost:18082/permission-manage | 372 | [admin-permission-manage.png](./admin-permission-manage.png) |  |
| PASS | admin | `/roles` | http://localhost:18082/permission-manage | 372 | [admin-roles.png](./admin-roles.png) |  |
| PASS | admin | `/admins` | http://localhost:18082/permission-manage | 372 | [admin-admins.png](./admin-admins.png) |  |
| PASS | admin | `/profile` | http://localhost:18082/profile | 155 | [admin-profile.png](./admin-profile.png) |  |

## In-Page Anchors

| Kind | Text | Href |
| --- | --- | --- |
| shop | 优品|品质生活 | http://localhost:18095/ |
| shop | 购物车 | http://localhost:18095/cart |
| shop | 我的订单 | http://localhost:18095/orders |
| shop | 限时秒杀 | http://localhost:18095/seckill |
| shop | 查看更多 › | http://localhost:18095/products?sort=sales_desc |
| shop | 查看更多 › | http://localhost:18095/products?sort=newest |
| admin | 管理后台 | http://localhost:18082/dashboard |
