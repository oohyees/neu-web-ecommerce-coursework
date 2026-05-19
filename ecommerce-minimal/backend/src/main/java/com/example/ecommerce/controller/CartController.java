package com.example.ecommerce.controller;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.CartMapper;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {
    private final CartMapper cartMapper;
    public CartController(CartMapper cartMapper) { this.cartMapper = cartMapper; }

    @PostMapping("/items")
    public ApiResponse<?> add(@RequestBody Map<String, Object> body) {
        cartMapper.upsert(Long.valueOf(String.valueOf(body.get("userId"))), Long.valueOf(String.valueOf(body.get("productId"))),
                body.get("specText")==null?null:String.valueOf(body.get("specText")), Integer.valueOf(String.valueOf(body.get("quantity"))));
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam Long userId) {
        return ApiResponse.ok(cartMapper.findByUserId(userId));
    }

    @PutMapping("/items")
    public ApiResponse<?> update(@RequestBody Map<String, Long> body) {
        cartMapper.updateQuantity(body.get("userId"), body.get("productId"), body.get("quantity").intValue());
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/items")
    public ApiResponse<?> delete(@RequestParam Long userId, @RequestParam Long productId) {
        cartMapper.deleteItem(userId, productId);
        return ApiResponse.ok(null);
    }
}
