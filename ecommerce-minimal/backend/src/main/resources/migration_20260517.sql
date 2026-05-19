ALTER TABLE product_category ADD COLUMN parent_id BIGINT NULL AFTER id;
ALTER TABLE cart_item ADD COLUMN spec_text VARCHAR(255) NULL AFTER product_id;
ALTER TABLE cart_item DROP INDEX uk_user_product;
ALTER TABLE cart_item ADD UNIQUE KEY uk_user_product_spec (user_id, product_id, spec_text);
ALTER TABLE orders ADD COLUMN coupon_id BIGINT NULL AFTER refund_status;
ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER coupon_id;
ALTER TABLE orders ADD COLUMN payment_method VARCHAR(32) NULL AFTER discount_amount;
ALTER TABLE order_item ADD COLUMN spec_text VARCHAR(255) NULL AFTER product_name;
ALTER TABLE admin_user ADD COLUMN nickname VARCHAR(64) NULL AFTER password;
ALTER TABLE admin_user ADD COLUMN email VARCHAR(128) NULL AFTER nickname;
ALTER TABLE admin_user ADD COLUMN phone VARCHAR(32) NULL AFTER email;
ALTER TABLE admin_user ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'ADMIN' AFTER phone;
ALTER TABLE promotion ADD COLUMN promotion_price DECIMAL(10,2) NULL AFTER promotion_type;
ALTER TABLE promotion ADD COLUMN promotion_stock INT NULL AFTER promotion_price;

CREATE TABLE IF NOT EXISTS user_coupon (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  coupon_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'UNUSED',
  claimed_at DATETIME NOT NULL,
  used_at DATETIME,
  UNIQUE KEY uk_user_coupon (user_id, coupon_id)
);

CREATE TABLE IF NOT EXISTS activity_notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  content VARCHAR(500) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS customer_consultation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  subject VARCHAR(128) NOT NULL,
  content VARCHAR(500) NOT NULL,
  reply VARCHAR(500),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL
);
