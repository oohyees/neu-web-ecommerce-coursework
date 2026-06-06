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
  name VARCHAR(128) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL,
  sales INT NOT NULL DEFAULT 0,
  is_on_sale TINYINT NOT NULL DEFAULT 1,
  image_url VARCHAR(255),
  detail_html TEXT,
  params_text VARCHAR(500)
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
