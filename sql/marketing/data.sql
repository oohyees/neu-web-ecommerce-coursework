SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

USE ecommerce_product;
-- ============================================================
-- marketing 域：种子数据
-- ============================================================

INSERT INTO coupon (name, threshold_amount, discount_amount, enabled) VALUES
('满300减30', 300.00, 30.00, 1),
('满500减80', 500.00, 80.00, 1);

INSERT INTO user_coupon (user_id, coupon_id, status, claimed_at) VALUES
(1, 1, 'UNUSED', NOW());

INSERT INTO promotion (product_id, title, promotion_type, promotion_price, promotion_stock, start_at, end_at, enabled) VALUES
(1, '限时优惠', 'FLASH_SALE', 75.99, 10, DATE_SUB(NOW(), INTERVAL 1 DAY), GREATEST('2026-12-31 23:59:59', DATE_ADD(NOW(), INTERVAL 180 DAY)), 1),
(2, '精选直降', 'PROMOTION', 53.20, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), GREATEST('2026-12-31 23:59:59', DATE_ADD(NOW(), INTERVAL 180 DAY)), 1);
