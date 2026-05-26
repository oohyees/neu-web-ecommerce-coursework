package com.example.ecommerce.order;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class OrderController {
    private final JdbcTemplate jdbc;
    public OrderController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/orders")
    public ApiResponse<?> orders(@RequestParam Long userId, @RequestParam(required = false) String status) {
        if (status == null || status.isBlank()) return ApiResponse.ok(jdbc.queryForList("select * from orders where user_id=? order by created_at desc,id desc", userId));
        return ApiResponse.ok(jdbc.queryForList("select * from orders where user_id=? and status=? order by created_at desc,id desc", userId, status));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        var order = jdbc.queryForMap("select * from orders where id=?", id);
        var items = jdbc.queryForList("select * from order_item where order_id=? order by id", id);
        var logistics = jdbc.queryForList("select * from order_logistics where order_id=? order by created_at,id", id);
        return ApiResponse.ok(Map.of("order", order, "items", items, "logistics", logistics));
    }

    @GetMapping("/cart")
    public ApiResponse<?> cart(@RequestParam Long userId) {
        return ApiResponse.ok(jdbc.queryForList("select c.id,p.name product_name,c.spec_text,p.price,c.quantity,p.price*c.quantity subtotal from cart_item c join product p on p.id=c.product_id where c.user_id=? order by c.id", userId));
    }
}
