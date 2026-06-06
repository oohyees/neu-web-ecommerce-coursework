package com.example.ecommerce.controller;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.CurrentSession;
import com.example.ecommerce.mapper.CartMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {
    private final CartMapper cartMapper;
    public CartController(CartMapper cartMapper) { this.cartMapper = cartMapper; }

    @PostMapping("/items")
    public ApiResponse<?> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        cartMapper.upsert(CurrentSession.userId(request), Long.valueOf(String.valueOf(body.get("productId"))),
                body.get("specText")==null?null:String.valueOf(body.get("specText")), Integer.valueOf(String.valueOf(body.get("quantity"))));
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<?> list(HttpServletRequest request) {
        return ApiResponse.ok(cartMapper.findByUserId(CurrentSession.userId(request)));
    }

    @PutMapping("/items")
    public ApiResponse<?> update(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long cartItemId = body.get("cartItemId") == null && body.get("id") == null ? null : Long.valueOf(String.valueOf(body.getOrDefault("cartItemId", body.get("id"))));
        Long productId = body.get("productId") == null ? null : Long.valueOf(String.valueOf(body.get("productId")));
        String specText = body.get("specText") == null ? null : String.valueOf(body.get("specText"));
        cartMapper.updateQuantity(CurrentSession.userId(request), cartItemId, productId, specText, Integer.valueOf(String.valueOf(body.get("quantity"))));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/items")
    public ApiResponse<?> delete(@RequestParam(required = false) Long cartItemId,
                                 @RequestParam(required = false) Long productId,
                                 @RequestParam(required = false) String specText,
                                 HttpServletRequest request) {
        cartMapper.deleteItem(CurrentSession.userId(request), cartItemId, productId, specText);
        return ApiResponse.ok(null);
    }
}
