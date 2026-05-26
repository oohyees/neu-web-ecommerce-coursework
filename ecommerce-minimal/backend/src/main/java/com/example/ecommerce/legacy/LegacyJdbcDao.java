package com.example.ecommerce.legacy;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class LegacyJdbcDao {
    private final DataSource dataSource;

    public LegacyJdbcDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> dashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("userCount", queryLong("select count(*) from user"));
        stats.put("productCount", queryLong("select count(*) from product"));
        stats.put("orderCount", queryLong("select count(*) from orders"));
        stats.put("totalSales", queryDecimal("select coalesce(sum(total_amount),0) from orders where payment_status='PAID'"));
        stats.put("lowStockCount", queryLong("select count(*) from product where stock < 10"));
        return stats;
    }

    private long queryLong(String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (Exception ex) {
            throw new IllegalStateException("JDBC 查询失败：" + sql, ex);
        }
    }

    private BigDecimal queryDecimal(String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (Exception ex) {
            throw new IllegalStateException("JDBC 查询失败：" + sql, ex);
        }
    }
}
