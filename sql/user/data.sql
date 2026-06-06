USE ecommerce_auth;
-- ============================================================
-- user 域：种子数据
-- ============================================================

INSERT INTO user (username, password, nickname, email, phone, avatar_url, enabled) VALUES
('alice', '123456', 'Alice', 'alice@example.com', '13800000000', '/catalog/avatar-alice.svg', 1);

INSERT INTO admin_user (username, password, nickname, email, phone, role) VALUES
('admin', 'admin123', '系统管理员', 'admin@example.com', '13900000000', 'SUPER_ADMIN'),
('operator', 'operator123', '普通管理员', 'operator@example.com', '13900000001', 'ADMIN'),
('manager', 'manager123', '演示普通管理员', 'manager@example.com', '13900000002', 'ADMIN');

INSERT INTO admin_permission (id, code, name, group_name) VALUES
(1,  'dashboard:view',     '查看数据看板',   '数据看板'),
(2,  'product:manage',     '商品管理',       '商品管理'),
(3,  'category:manage',    '分类管理',       '商品管理'),
(4,  'review:manage',      '评价管理',       '商品管理'),
(5,  'order:manage',       '订单管理',       '订单管理'),
(6,  'order:export',       '订单导出',       '订单管理'),
(7,  'user:manage',        '用户管理',       '用户管理'),
(8,  'banner:manage',      '轮播管理',       '内容管理'),
(9,  'announcement:manage','公告管理',       '内容管理'),
(10, 'activity:manage',    '活动通知管理',   '内容管理'),
(11, 'feedback:manage',    '反馈管理',       '内容管理'),
(12, 'consultation:manage','咨询管理',       '内容管理'),
(13, 'coupon:manage',      '优惠券管理',     '营销管理'),
(14, 'promotion:manage',   '促销管理',       '营销管理'),
(15, 'admin:manage',       '管理员管理',     '系统管理'),
(16, 'import:product',     '商品导入',       '数据管理'),
(17, 'export:product',     '商品导出',       '数据管理'),
(18, 'export:stats',       '统计导出',       '数据管理');

INSERT INTO admin_role_permission (role, permission_id)
SELECT 'SUPER_ADMIN', id FROM admin_permission;

INSERT INTO admin_role_permission (role, permission_id)
SELECT 'ADMIN', id FROM admin_permission WHERE code != 'admin:manage';
