package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.MarketingMapper;
import com.example.ecommerce.model.Promotion;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
@RestController @RequestMapping("/api/marketing") @CrossOrigin
public class MarketingController {
    private final MarketingMapper mapper;
    public MarketingController(MarketingMapper mapper){this.mapper=mapper;}
    @GetMapping("/specs/{productId}") public ApiResponse<?> specs(@PathVariable Long productId){return ApiResponse.ok(mapper.findSpecs(productId));}
    @GetMapping("/coupons") public ApiResponse<?> coupons(){return ApiResponse.ok(mapper.findCoupons());}
    @GetMapping("/coupons/user/{userId}") public ApiResponse<?> userCoupons(@PathVariable Long userId){return ApiResponse.ok(mapper.findUserCoupons(userId));}
    @PostMapping("/coupons/{couponId}/claim") public ApiResponse<?> claim(@PathVariable Long couponId,@RequestParam Long userId){mapper.claimCoupon(userId,couponId);return ApiResponse.ok(null);}
    @GetMapping("/promotions") public ApiResponse<?> promotions(){return ApiResponse.ok(mapper.findPromotions());}

    // 管理员促销管理
    @GetMapping("/admin/promotions")
    public ApiResponse<?> adminPromotions(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(mapper.findAllPromotions(keyword));
    }
    @PostMapping("/admin/promotions")
    public ApiResponse<?> createPromotion(@RequestBody Promotion p) {
        if (p.getStartAt() == null) p.setStartAt(LocalDateTime.now());
        if (p.getEndAt() == null) p.setEndAt(LocalDateTime.now().plusDays(7));
        if (p.getEnabled() == null) p.setEnabled(true);
        mapper.insertPromotion(p);
        return ApiResponse.ok(p);
    }
    @PutMapping("/admin/promotions")
    public ApiResponse<?> updatePromotion(@RequestBody Promotion p) {
        mapper.updatePromotion(p);
        return ApiResponse.ok(p);
    }
    @DeleteMapping("/admin/promotions/{id}")
    public ApiResponse<?> deletePromotion(@PathVariable Long id) {
        mapper.deletePromotion(id);
        return ApiResponse.ok(null);
    }
}
