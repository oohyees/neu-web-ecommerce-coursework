-- MySQL 初始化：创建数据库与应用账号
CREATE DATABASE IF NOT EXISTS ecommerce_minimal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ecommerce'@'%' IDENTIFIED BY 'ecommerce123';
GRANT ALL PRIVILEGES ON ecommerce_minimal.* TO 'ecommerce'@'%';
FLUSH PRIVILEGES;
