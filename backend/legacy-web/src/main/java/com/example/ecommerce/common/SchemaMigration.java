package com.example.ecommerce.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class SchemaMigration {
    private final JdbcTemplate jdbc;

    public SchemaMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void migrate() {
        addColumnIfMissing("feedback", "type", "varchar(32)");
        addColumnIfMissing("feedback", "contact", "varchar(128)");
        ensurePermissionTables();
        ensureHotSearchTable();
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        Integer tableCount = jdbc.queryForObject("""
                select count(*)
                from information_schema.tables
                where table_schema = database()
                  and table_name = ?
                """, Integer.class, tableName);
        if (tableCount == null || tableCount == 0) {
            return;
        }
        Integer count = jdbc.queryForObject("""
                select count(*)
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """, Integer.class, tableName, columnName);
        if (count != null && count == 0) {
            jdbc.execute("alter table " + tableName + " add column " + columnName + " " + definition);
        }
    }

    private void ensureHotSearchTable() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS hot_search (
              id BIGINT PRIMARY KEY AUTO_INCREMENT,
              keyword VARCHAR(100) NOT NULL UNIQUE,
              search_count INT DEFAULT 0,
              enabled TINYINT DEFAULT 1,
              sort_order INT DEFAULT 0
            )
        """);
    }

    private void ensurePermissionTables() {
        // admin_permission table
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS admin_permission (
              id BIGINT PRIMARY KEY AUTO_INCREMENT,
              code VARCHAR(64) NOT NULL UNIQUE,
              name VARCHAR(64) NOT NULL,
              group_name VARCHAR(64)
            )
        """);
        // admin_role_permission table
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS admin_role_permission (
              id BIGINT PRIMARY KEY AUTO_INCREMENT,
              role VARCHAR(32) NOT NULL,
              permission_id BIGINT NOT NULL,
              UNIQUE KEY uk_role_perm (role, permission_id)
            )
        """);
        // Seed default permissions if table is empty
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM admin_permission", Integer.class);
        if (count != null && count == 0) {
            jdbc.execute("""
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
                (18, 'export:stats',       '统计导出',       '数据管理')
            """);
            jdbc.execute("""
                INSERT INTO admin_role_permission (role, permission_id)
                SELECT 'SUPER_ADMIN', id FROM admin_permission
            """);
            jdbc.execute("""
                INSERT INTO admin_role_permission (role, permission_id)
                SELECT 'ADMIN', id FROM admin_permission WHERE code != 'admin:manage'
            """);
        }
    }
}
