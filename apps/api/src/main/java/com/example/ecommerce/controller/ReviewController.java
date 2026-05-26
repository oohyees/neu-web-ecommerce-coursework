package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.CurrentSession;
import com.example.ecommerce.mapper.ReviewMapper;
import com.example.ecommerce.model.ProductReview;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
@RestController @RequestMapping("/api/reviews") @CrossOrigin
public class ReviewController {
    private final ReviewMapper mapper;
    public ReviewController(ReviewMapper mapper){this.mapper=mapper;}
    @GetMapping public ApiResponse<?> list(@RequestParam Long productId){return ApiResponse.ok(mapper.findByProductId(productId));}
    @PostMapping public ApiResponse<?> create(@RequestBody ProductReview review, HttpServletRequest request){review.setUserId(CurrentSession.userId(request));review.setCreatedAt(LocalDateTime.now());mapper.insert(review);return ApiResponse.ok(review);}
    @GetMapping("/admin/all") public ApiResponse<?> all(@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer size){var all=mapper.findAll();int from=Math.min((page-1)*size,all.size()),to=Math.min(from+size,all.size());return ApiResponse.ok(java.util.Map.of("items",all.subList(from,to),"total",all.size()));}
    @DeleteMapping("/admin/{id}") public ApiResponse<?> delete(@PathVariable Long id){mapper.delete(id);return ApiResponse.ok(null);}
}
