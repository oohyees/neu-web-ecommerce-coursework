UPDATE admin_user SET role='SUPER_ADMIN' WHERE username='admin';

INSERT INTO activity_notice (title, content, enabled, created_at)
SELECT '618 预热活动', '指定商品限时直降，优惠券可叠加使用。', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM activity_notice);

INSERT INTO customer_consultation (user_id, subject, content, reply, status, created_at)
SELECT 1, '发货时效', '下单后通常多久发货？', '工作日订单通常在 24 小时内发出。', 'REPLIED', NOW()
WHERE EXISTS (SELECT 1 FROM user WHERE id=1)
  AND NOT EXISTS (SELECT 1 FROM customer_consultation);

INSERT INTO coupon (name, threshold_amount, discount_amount, enabled)
SELECT '满300减30', 300.00, 30.00, 1
WHERE NOT EXISTS (SELECT 1 FROM coupon);

INSERT INTO promotion (product_id, title, promotion_type, promotion_price, promotion_stock, start_at, end_at, enabled)
SELECT id, '限时优惠', 'FLASH_SALE', price - 10, 10, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 1
FROM product
WHERE NOT EXISTS (SELECT 1 FROM promotion)
ORDER BY id
LIMIT 1;

INSERT INTO product_spec (product_id, spec_name, spec_value)
SELECT id, '默认规格', '标准版'
FROM product
WHERE NOT EXISTS (SELECT 1 FROM product_spec)
ORDER BY id
LIMIT 1;

UPDATE user SET avatar_url='/catalog/avatar-alice.svg' WHERE id=1 AND (avatar_url IS NULL OR avatar_url='');
UPDATE product SET detail_html=COALESCE(detail_html,'暂无详情'), params_text=COALESCE(params_text,'暂无参数');
