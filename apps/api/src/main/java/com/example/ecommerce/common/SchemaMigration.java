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
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
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
}
