# Fullstack UI Audit Evidence - 2026-06-07

入口：

- 前台：http://localhost:5173
- 后台：http://localhost:5174
- Gateway：http://localhost:18090

## 截图索引

| 状态 | 截图 | 说明 |
| --- | --- | --- |
| PASS | shop-home-hot-products-visible.png | visible text: 热门好物 |
| PASS | shop-01-home.png | 首页：轮播、分类导航、热门商品、新品/促销入口 |
| PASS | shop-search-title-visible.png | visible text: 商品搜索 |
| PASS | shop-02-search-fuzzy-sort.png | 商品搜索：关键词、模糊搜索、排序控件、商品列表 |
| PASS | shop-category-visible.png | visible text: 分类 |
| PASS | shop-03-category.png | 分类浏览：按分类筛选商品 |
| PASS | shop-product-add-cart-visible.png | visible text: 加入购物车 |
| PASS | shop-04-product-detail-top.png | 商品详情：图片、名称、价格、库存、参数、规格、加购 |
| PASS | shop-04-product-detail-reviews.png | 商品详情评价区：查看评价、提交评价入口 |
| PASS | shop-register-email-code-visible.png | visible text: 邮箱验证码 |
| PASS | shop-06-register.png | 注册页：邮箱注册、验证码、手机号、密码 |
| PASS | shop-forgot-code-visible.png | visible text: 获取验证码 |
| PASS | shop-07-forgot-password.png | 找回密码：邮箱验证码、重置密码 |
| PASS | shop-05-login.png | 用户登录页：账号密码登录、记住密码、注册/找回入口 |
| PASS | shop-profile-upload-visible.png | visible text: 上传图片 |
| PASS | shop-12-profile.png | 个人中心：头像、昵称、邮箱、手机号、密码修改 |
| PASS | shop-address-add-visible.png | visible text: 新增地址 |
| PASS | shop-13-address-list.png | 收货地址：列表、默认地址、新增/编辑/删除入口 |
| PASS | shop-13-address-add-dialog.png | 收货地址：新增地址弹窗 |
| PASS | shop-favorites-visible.png | visible text: 我的收藏 |
| PASS | shop-14-favorites.png | 收藏列表：收藏/取消收藏结果查看 |
| PASS | shop-coupons-visible.png | visible text: 优惠券 |
| PASS | shop-15-coupons.png | 优惠券：可领取/已领取、结算联动说明 |
| PASS | shop-seckill-visible.png | visible text: 限时 |
| PASS | shop-16-seckill.png | 秒杀/促销活动页 |
| PASS | shop-notices-visible.png | visible text: 公告 |
| PASS | shop-17-notices.png | 公告/活动通知展示 |
| PASS | shop-feedback-visible.png | visible text: 意见反馈 |
| PASS | shop-18-feedback.png | 用户反馈：问题/建议提交与历史列表 |
| PASS | shop-service-visible.png | visible text: 客服 |
| PASS | shop-19-service.png | 客服咨询：留言/回复/聊天入口 |
| PASS | shop-cart-checkout-visible.png | visible text: 去结算 |
| PASS | shop-08-cart.png | 购物车：图片、名称、单价、数量、小计、勾选、全选/反选、删除、结算 |
| PASS | shop-checkout-submit-visible.png | visible text: 提交订单 |
| PASS | shop-09-checkout.png | 确认订单：地址、商品清单、优惠券、总价、提交订单 |
| PASS | shop-payment-visible.png | visible text: 订单支付 |
| PASS | shop-10-payment.png | 支付页：模拟支付、金额、支付方式、确认支付 |
| PASS | shop-orders-visible.png | visible text: 我的订单 |
| PASS | shop-11-orders.png | 我的订单：全部/待支付/待发货/待收货/已完成/已取消、支付/取消/收货/退款/物流入口 |
| PASS | admin-01-login.png | 管理员登录页 |
| PASS | admin-02-dashboard.png | 数据看板：总用户、总订单、总销售额、今日订单/销售额、图表 |
| PASS | admin-03-products.png | 商品管理：列表、搜索、分类/状态筛选、新增、编辑、上下架、删除、导入导出、上传图入口 |
| PASS | admin-04-categories.png | 分类管理：新增、编辑、删除、排序字段 |
| PASS | admin-05-orders.png | 订单管理：订单号/用户/状态筛选、支付/履约状态、发货/退款、导出 |
| PASS | admin-06-users.png | 用户管理：列表、搜索、启用/禁用 |
| PASS | admin-07-reviews.png | 评价管理：评价列表、删除违规评价 |
| PASS | admin-08-banners.png | 轮播管理：列表、新增、编辑、删除、上传、链接、排序 |
| PASS | admin-09-promotions.png | 促销/优惠券管理：新增、编辑、删除、启停 |
| PASS | admin-10-notices.png | 公告/活动通知管理：发布、编辑、删除 |
| PASS | admin-11-feedbacks.png | 反馈管理：查看、回复、标记处理 |
| PASS | admin-12-cs.png | 客服咨询：查看用户咨询、回复 |
| PASS | admin-13-permission.png | 权限管理：管理员账号、角色权限分级 |
| PASS | admin-14-profile.png | 管理员个人中心：资料、密码修改 |
| PASS | admin-03-products-add-dialog.png | 商品新增弹窗：名称、分类、价格、库存、图片上传、详情 |
| PASS | admin-08-banners-add-dialog.png | 轮播新增弹窗：图片、链接、排序、上传 |
| PASS | admin-09-coupons-tab.png | 优惠券管理：列表、新增、编辑、删除、启停 |
| PASS | admin-09-coupons-add-dialog.png | 优惠券新增弹窗：名称、满减门槛、优惠金额、启用 |
| PASS | responsive-shop-390-home.png | 390px 前台首页响应式 |
| PASS | responsive-shop-390-search.png | 390px 前台商品列表响应式 |
| PASS | responsive-shop-768-home.png | 768px 前台首页响应式 |
| PASS | admin-01-login.png | 管理员登录页 |
| PASS | responsive-admin-390-dashboard.png | 390px 后台看板响应式 |
| PASS | responsive-admin-390-drawer.png | 390px 后台抽屉菜单 |

## 控制台问题

- 未捕获到 pageerror；控制台 warning/error 见脚本过滤结果为空。

## HTTP 失败响应

- 未捕获到 HTTP 4xx/5xx 响应。
