SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS ecommerce_minimal DEFAULT CHARACTER SET utf8mb4;
USE ecommerce_minimal;

DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS order_logistics;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS user_coupon;
DROP TABLE IF EXISTS product_review;
DROP TABLE IF EXISTS product_favorite;
DROP TABLE IF EXISTS banner;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS activity_notice;
DROP TABLE IF EXISTS customer_consultation;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS user_address;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS product_spec;
DROP TABLE IF EXISTS promotion;
DROP TABLE IF EXISTS coupon;
DROP TABLE IF EXISTS verification_code;
DROP TABLE IF EXISTS product_category;
DROP TABLE IF EXISTS admin_user;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  email VARCHAR(128),
  phone VARCHAR(32),
  avatar_url VARCHAR(255),
  enabled TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE admin_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  nickname VARCHAR(64),
  email VARCHAR(128),
  phone VARCHAR(32),
  role VARCHAR(32) NOT NULL DEFAULT 'ADMIN'
);

CREATE TABLE product (
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

CREATE TABLE product_spec (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  spec_name VARCHAR(64) NOT NULL,
  spec_value VARCHAR(128) NOT NULL
);

CREATE TABLE user_coupon (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  coupon_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'UNUSED',
  claimed_at DATETIME NOT NULL,
  used_at DATETIME,
  UNIQUE KEY uk_user_coupon (user_id, coupon_id)
);

CREATE TABLE coupon (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  threshold_amount DECIMAL(10,2) NOT NULL,
  discount_amount DECIMAL(10,2) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE promotion (
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

CREATE TABLE verification_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(128) NOT NULL,
  code VARCHAR(16) NOT NULL,
  purpose VARCHAR(32) NOT NULL,
  expires_at DATETIME NOT NULL
);

CREATE TABLE product_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT,
  name VARCHAR(64) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE banner (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  link_url VARCHAR(255),
  sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE product_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_product_favorite (user_id, product_id)
);

CREATE TABLE product_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content VARCHAR(500) NOT NULL,
  image_url VARCHAR(255),
  created_at DATETIME NOT NULL
);

CREATE TABLE cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  spec_text VARCHAR(255),
  quantity INT NOT NULL,
  UNIQUE KEY uk_user_product_spec (user_id, product_id, spec_text)
);

CREATE TABLE user_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  receiver_name VARCHAR(64) NOT NULL,
  phone VARCHAR(32) NOT NULL,
  province VARCHAR(64) NOT NULL,
  city VARCHAR(64) NOT NULL,
  district VARCHAR(64) NOT NULL,
  detail_address VARCHAR(255) NOT NULL,
  is_default TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  address_id BIGINT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  payment_status VARCHAR(32) NOT NULL DEFAULT 'UNPAID',
  logistics_status VARCHAR(64),
  refund_status VARCHAR(32) DEFAULT 'NONE',
  coupon_id BIGINT,
  discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
  payment_method VARCHAR(32),
  created_at DATETIME NOT NULL
);

CREATE TABLE announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  content VARCHAR(500) NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE activity_notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  content VARCHAR(500) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL
);

CREATE TABLE customer_consultation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  subject VARCHAR(128) NOT NULL,
  content VARCHAR(500) NOT NULL,
  reply VARCHAR(500),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL
);

CREATE TABLE feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(32),
  content VARCHAR(500) NOT NULL,
  contact VARCHAR(128),
  reply VARCHAR(500),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL
);

CREATE TABLE order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(128) NOT NULL,
  spec_text VARCHAR(255),
  unit_price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL
);

CREATE TABLE order_logistics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  content VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL
);
