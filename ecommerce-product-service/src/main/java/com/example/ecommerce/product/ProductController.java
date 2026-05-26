package com.example.ecommerce.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final JdbcTemplate jdbc;
    public ProductController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        String where = keyword == null || keyword.isBlank() ? " where is_on_sale=1" : " where is_on_sale=1 and name like ?";
        Object[] args = keyword == null || keyword.isBlank() ? new Object[]{} : new Object[]{"%" + keyword + "%"};
        var items = jdbc.queryForList("select id,name,price,stock,sales,image_url from product" + where + " order by id desc limit " + size + " offset " + ((page - 1) * size), args);
        var total = jdbc.queryForObject("select count(*) from product" + where, Integer.class, args);
        return ApiResponse.ok(Map.of("items", items, "total", total == null ? 0 : total));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.ok(jdbc.queryForMap("select * from product where id=?", id));
    }

    @GetMapping("/categories")
    public ApiResponse<?> categories() {
        return ApiResponse.ok(jdbc.queryForList("select * from product_category order by sort_order,id"));
    }
}
