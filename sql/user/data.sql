-- ============================================================
-- user 域：种子数据
-- ============================================================

INSERT INTO user (username, password, nickname, email, phone, avatar_url, enabled) VALUES
('alice', '123456', 'Alice', 'alice@example.com', '13800000000', '/catalog/avatar-alice.svg', 1);

INSERT INTO admin_user (username, password, nickname, email, phone, role) VALUES
('admin', 'admin123', '系统管理员', 'admin@example.com', '13900000000', 'SUPER_ADMIN'),
('operator', 'operator123', '普通管理员', 'operator@example.com', '13900000001', 'ADMIN'),
('manager', 'manager123', '演示普通管理员', 'manager@example.com', '13900000002', 'ADMIN');
