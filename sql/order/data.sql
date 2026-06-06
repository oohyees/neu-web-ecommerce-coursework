-- ============================================================
-- order 域：种子数据
-- ============================================================

INSERT INTO user_address (user_id, receiver_name, phone, province, city, district, detail_address, is_default) VALUES
(1, 'Alice', '13800000000', '辽宁省', '沈阳市', '和平区', '创新路 1 号', 1);

INSERT INTO cart_item (user_id, product_id, spec_text, quantity) VALUES
(1, 2, '品牌:Razer|型号:RZ01-03210300-R3M1', 2),
(1, 4, '品牌:Allsop|型号:ASP30203', 1);

INSERT INTO orders (order_no, user_id, address_id, total_amount, status, payment_status, payment_method, logistics_status, refund_status, created_at) VALUES
('ORD202605190001', 1, 1, 174.19, 'CREATED', 'UNPAID', 'MOCK_PAY', '待支付', 'NONE', '2026-05-19 09:30:00');

INSERT INTO order_item (order_id, product_id, product_name, spec_text, unit_price, quantity, subtotal) VALUES
(1, 1, 'Koolertron 单手机械键盘', '品牌:Koolertron|型号:AE-SMKD7', 85.99, 2, 171.98),
(1, 2, 'Razer DeathAdder V2 电竞鼠标', '品牌:Razer|型号:RZ01-03210300-R3M1', 58.20, 1, 58.20);

INSERT INTO order_logistics (order_id, content, created_at) VALUES
(1, '订单已创建', '2026-05-19 09:30:00');

INSERT INTO orders (order_no, user_id, address_id, total_amount, status, payment_status, payment_method, logistics_status, refund_status, created_at) VALUES
('ORD202605190002', 1, 1, 389.00, 'CREATED', 'PAID', 'ALIPAY', '待发货', 'NONE', '2026-05-19 14:20:00');

INSERT INTO order_item (order_id, product_id, product_name, spec_text, unit_price, quantity, subtotal) VALUES
(2, 11, 'Sony XP500 便携派对音箱', '品牌:Sony|型号:SRSXP500', 389.00, 1, 389.00);

INSERT INTO order_logistics (order_id, content, created_at) VALUES
(2, '订单已创建', '2026-05-19 14:20:00'),
(2, '订单已支付', '2026-05-19 14:22:00');

INSERT INTO orders (order_no, user_id, address_id, total_amount, status, payment_status, payment_method, logistics_status, refund_status, created_at) VALUES
('ORD202605180003', 1, 1, 95.98, 'SHIPPED', 'PAID', 'WECHAT', '运输中', 'NONE', '2026-05-18 10:05:00');

INSERT INTO order_item (order_id, product_id, product_name, spec_text, unit_price, quantity, subtotal) VALUES
(3, 8, 'EKSA AI 降噪蓝牙耳麦', '品牌:EKSA|型号:H1', 84.99, 1, 84.99),
(3, 4, 'Allsop 记忆棉护腕鼠标垫', '品牌:Allsop|型号:ASP30203', 10.99, 1, 10.99);

INSERT INTO order_logistics (order_id, content, created_at) VALUES
(3, '订单已创建', '2026-05-18 10:05:00'),
(3, '订单已支付', '2026-05-18 10:06:00'),
(3, '商家已发货，快递单号 SF1234567890', '2026-05-18 15:30:00'),
(3, '快件已到达沈阳分拣中心', '2026-05-19 08:00:00');
