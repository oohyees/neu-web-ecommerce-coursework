SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE ecommerce_product;
-- ============================================================
-- content 域：种子数据
-- ============================================================

INSERT INTO banner (title, image_url, link_url, sort_order) VALUES
('Essence 睫毛膏 限时特惠', 'https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/1.webp', '/products/5000', 1),
('Dior J''adore 香水 新品上市', 'https://cdn.dummyjson.com/product-images/fragrances/dior-j''adore/thumbnail.webp', '/products/5007', 2),
('Annibale Colombo 精品沙发', 'https://cdn.dummyjson.com/product-images/furniture/annibale-colombo-sofa/1.webp', '/products/5011', 3),
('Calvin Klein 经典香水', 'https://cdn.dummyjson.com/product-images/fragrances/calvin-klein-ck-one/1.webp', '/products/5005', 4),
('家居相框 畅玩无限', 'https://cdn.dummyjson.com/product-images/home-decoration/family-tree-photo-frame/thumbnail.webp', '/products/5043', 5),
('Samsung Galaxy S8', 'https://cdn.dummyjson.com/product-images/smartphones/samsung-galaxy-s8/1.webp', '/products/5044', 6),
('高品质厨房好物', 'https://cdn.dummyjson.com/product-images/kitchen-accessories/silver-pot-with-glass-cap/thumbnail.webp', '/products/5070', 7),
('Apple iPhone X', 'https://cdn.dummyjson.com/product-images/smartphones/iphone-x/1.webp', '/products/5046', 8),
('厨房削皮器 创意利器', 'https://cdn.dummyjson.com/product-images/kitchen-accessories/yellow-peeler/thumbnail.webp', '/products/5076', 9);

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
