USE ecommerce_auth;
-- ============================================================
-- 用户域：用户、管理员、验证码
-- ============================================================

CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  email VARCHAR(128),
  phone VARCHAR(32),
  avatar_url VARCHAR(255),
  enabled TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS admin_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  nickname VARCHAR(64),
  email VARCHAR(128),
  phone VARCHAR(32),
  role VARCHAR(32) NOT NULL DEFAULT 'ADMIN'
);

CREATE TABLE IF NOT EXISTS verification_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(128) NOT NULL,
  code VARCHAR(16) NOT NULL,
  purpose VARCHAR(32) NOT NULL,
  expires_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS admin_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  group_name VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS admin_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role VARCHAR(32) NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_perm (role, permission_id)
);
