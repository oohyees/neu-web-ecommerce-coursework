package com.example.ecommerce.admin;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final JdbcTemplate jdbc;
    public AdminController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard() {
        Map<String, Object> stats = Map.of(
                "users", jdbc.queryForObject("select count(*) from user", Integer.class),
                "orders", jdbc.queryForObject("select count(*) from orders", Integer.class),
                "products", jdbc.queryForObject("select count(*) from product", Integer.class),
                "sales", jdbc.queryForObject("select coalesce(sum(total_amount),0) from orders where payment_status='PAID'", java.math.BigDecimal.class)
        );
        return ApiResponse.ok(stats);
    }

    @GetMapping("/users")
    public ApiResponse<?> users(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) return ApiResponse.ok(jdbc.queryForList("select id,username,nickname,email,phone,enabled from user order by id desc"));
        return ApiResponse.ok(jdbc.queryForList("select id,username,nickname,email,phone,enabled from user where username like ? or nickname like ? order by id desc", "%" + keyword + "%", "%" + keyword + "%"));
    }

    @GetMapping("/orders")
    public ApiResponse<?> orders() {
        return ApiResponse.ok(jdbc.queryForList("select * from orders order by created_at desc,id desc limit 100"));
    }
}
