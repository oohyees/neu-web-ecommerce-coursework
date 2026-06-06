USE ecommerce_product;
-- ============================================================
-- 商品域：分类、商品、规格、评价、收藏
-- ============================================================

CREATE TABLE IF NOT EXISTS product_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT,
  name VARCHAR(64) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  name VARCHAR(256) NOT NULL,
  subtitle VARCHAR(512),
  price DECIMAL(10,2) NOT NULL,
  original_price DECIMAL(10,2),
  stock INT NOT NULL,
  sales INT NOT NULL DEFAULT 0,
  is_on_sale TINYINT NOT NULL DEFAULT 1,
  image_url VARCHAR(1024),
  detail_html MEDIUMTEXT,
  params_text VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS product_sku (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  sku_code VARCHAR(64) NOT NULL,
  color VARCHAR(32),
  size VARCHAR(32),
  price DECIMAL(10,2),
  stock INT DEFAULT 0,
  image VARCHAR(1024),
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS product_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  url VARCHAR(1024) NOT NULL,
  sort_order INT DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS product_spec (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  spec_name VARCHAR(64) NOT NULL,
  spec_value VARCHAR(128) NOT NULL
);

CREATE TABLE IF NOT EXISTS product_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content VARCHAR(500) NOT NULL,
  image_url VARCHAR(255),
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS product_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_product_favorite (user_id, product_id)
);
