package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.CurrentSession;
import com.example.ecommerce.mapper.FavoriteMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/favorites") @CrossOrigin
public class FavoriteController {
    private final FavoriteMapper mapper;
    public FavoriteController(FavoriteMapper mapper){this.mapper=mapper;}
    @PostMapping("/{productId}") public ApiResponse<?> add(@PathVariable Long productId,@RequestParam(required=false) Long userId, HttpServletRequest request){mapper.insert(CurrentSession.userId(request),productId);return ApiResponse.ok(null);}
    @DeleteMapping("/{productId}") public ApiResponse<?> remove(@PathVariable Long productId,@RequestParam(required=false) Long userId, HttpServletRequest request){mapper.delete(CurrentSession.userId(request),productId);return ApiResponse.ok(null);}
    @GetMapping("/{productId}/status") public ApiResponse<?> status(@PathVariable Long productId,@RequestParam(required=false) Long userId, HttpServletRequest request){return ApiResponse.ok(mapper.count(CurrentSession.userId(request),productId)>0);}
    @GetMapping public ApiResponse<?> list(@RequestParam(required=false) Long userId, HttpServletRequest request){return ApiResponse.ok(mapper.findByUserId(CurrentSession.userId(request)));}
}
