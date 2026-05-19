package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.FavoriteMapper;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/favorites") @CrossOrigin
public class FavoriteController {
    private final FavoriteMapper mapper;
    public FavoriteController(FavoriteMapper mapper){this.mapper=mapper;}
    @PostMapping("/{productId}") public ApiResponse<?> add(@PathVariable Long productId,@RequestParam Long userId){mapper.insert(userId,productId);return ApiResponse.ok(null);}
    @DeleteMapping("/{productId}") public ApiResponse<?> remove(@PathVariable Long productId,@RequestParam Long userId){mapper.delete(userId,productId);return ApiResponse.ok(null);}
    @GetMapping("/{productId}/status") public ApiResponse<?> status(@PathVariable Long productId,@RequestParam Long userId){return ApiResponse.ok(mapper.count(userId,productId)>0);}
    @GetMapping public ApiResponse<?> list(@RequestParam Long userId){return ApiResponse.ok(mapper.findByUserId(userId));}
}
