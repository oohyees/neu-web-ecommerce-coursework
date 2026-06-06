-- MySQL 初始化：为每个微服务创建独立数据库
CREATE DATABASE IF NOT EXISTS ecommerce_auth    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_product DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_order   DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_admin   DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ecommerce'@'%' IDENTIFIED BY 'ecommerce123';
GRANT ALL PRIVILEGES ON ecommerce_auth.*    TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_product.* TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_order.*   TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_admin.*   TO 'ecommerce'@'%';
FLUSH PRIVILEGES;
