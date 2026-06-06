-- ============================================================
-- 营销域：优惠券、促销
-- ============================================================

CREATE TABLE IF NOT EXISTS coupon (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  threshold_amount DECIMAL(10,2) NOT NULL,
  discount_amount DECIMAL(10,2) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS user_coupon (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  coupon_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'UNUSED',
  claimed_at DATETIME NOT NULL,
  used_at DATETIME,
  UNIQUE KEY uk_user_coupon (user_id, coupon_id)
);

CREATE TABLE IF NOT EXISTS promotion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  promotion_type VARCHAR(32) NOT NULL,
  promotion_price DECIMAL(10,2),
  promotion_stock INT,
  start_at DATETIME NOT NULL,
  end_at DATETIME NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1
);
