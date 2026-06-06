USE ecommerce_product;
-- ============================================================
-- content 域：种子数据
-- ============================================================

INSERT INTO banner (title, image_url, link_url, sort_order) VALUES
('机械键盘限时特惠', '/catalog/B076LRJ528.webp', '/products/1', 1),
('电竞鼠标办公升级', '/catalog/B08F2Z6RJB.webp', '/products/2', 2);

INSERT INTO announcement (title, content, created_at) VALUES
('真实商品目录已更新', '当前目录使用公开商品样本整理，商品图与商品信息均可追溯来源。', NOW());

INSERT INTO activity_notice (title, content, enabled, created_at) VALUES
('618 预热活动', '指定商品限时直降，优惠券可叠加使用。', 1, NOW());

INSERT INTO customer_consultation (user_id, subject, content, reply, status, created_at) VALUES
(1, '发货时效', '下单后通常多久发货？', '工作日订单通常在 24 小时内发出。', 'REPLIED', NOW());

INSERT INTO feedback (user_id, content, reply, status, created_at) VALUES
(1, '希望能增加更多支付方式，比如银联云闪付。', '感谢反馈，银联支付已在支付页面开放，欢迎使用。', 'REPLIED', NOW());

INSERT INTO hot_search (keyword, search_count, enabled, sort_order) VALUES
('机械键盘', 156, 1, 1),
('无线鼠标', 143, 1, 2),
('蓝牙耳机', 128, 1, 3),
('显示器', 112, 1, 4),
('Type-C数据线', 98, 1, 5);
