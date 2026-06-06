package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.CurrentSession;
import com.example.ecommerce.mapper.MarketingMapper;
import com.example.ecommerce.model.Coupon;
import com.example.ecommerce.model.Promotion;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
@RestController @RequestMapping("/api/marketing") @CrossOrigin
public class MarketingController {
    private final MarketingMapper mapper;
    public MarketingController(MarketingMapper mapper){this.mapper=mapper;}
    @GetMapping("/specs/{productId}") public ApiResponse<?> specs(@PathVariable Long productId){return ApiResponse.ok(mapper.findSpecs(productId));}
    @GetMapping("/coupons") public ApiResponse<?> coupons(){return ApiResponse.ok(mapper.findCoupons());}
    @GetMapping("/coupons/user/{userId}") public ApiResponse<?> userCoupons(@PathVariable(required = false) Long userId, HttpServletRequest request){return ApiResponse.ok(mapper.findUserCoupons(CurrentSession.userId(request)));}
    @PostMapping("/coupons/{couponId}/claim") public ApiResponse<?> claim(@PathVariable Long couponId,@RequestParam(required=false) Long userId, HttpServletRequest request){mapper.claimCoupon(CurrentSession.userId(request),couponId);return ApiResponse.ok(null);}
    @GetMapping("/promotions") public ApiResponse<?> promotions(){return ApiResponse.ok(mapper.findPromotions());}

    // 管理员优惠券管理
    @GetMapping("/admin/coupons")
    @RequiresPermissions("coupon:manage")
    public ApiResponse<?> adminCoupons(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(mapper.findAllCoupons(keyword));
    }
    @PostMapping("/admin/coupons")
    @RequiresPermissions("coupon:manage")
    public ApiResponse<?> createCoupon(@RequestBody Coupon c) {
        if (c.getEnabled() == null) c.setEnabled(true);
        mapper.insertCoupon(c);
        return ApiResponse.ok(c);
    }
    @PutMapping("/admin/coupons")
    @RequiresPermissions("coupon:manage")
    public ApiResponse<?> updateCoupon(@RequestBody Coupon c) {
        mapper.updateCoupon(c);
        return ApiResponse.ok(c);
    }
    @DeleteMapping("/admin/coupons/{id}")
    @RequiresPermissions("coupon:manage")
    public ApiResponse<?> deleteCoupon(@PathVariable Long id) {
        mapper.deleteCoupon(id);
        return ApiResponse.ok(null);
    }

    // 管理员促销管理
    @GetMapping("/admin/promotions")
    @RequiresPermissions("promotion:manage")
    public ApiResponse<?> adminPromotions(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(mapper.findAllPromotions(keyword));
    }
    @PostMapping("/admin/promotions")
    @RequiresPermissions("promotion:manage")
    public ApiResponse<?> createPromotion(@RequestBody Promotion p) {
        if (p.getStartAt() == null) p.setStartAt(LocalDateTime.now());
        if (p.getEndAt() == null) p.setEndAt(LocalDateTime.now().plusDays(7));
        if (p.getEnabled() == null) p.setEnabled(true);
        mapper.insertPromotion(p);
        return ApiResponse.ok(p);
    }
    @PutMapping("/admin/promotions")
    @RequiresPermissions("promotion:manage")
    public ApiResponse<?> updatePromotion(@RequestBody Promotion p) {
        mapper.updatePromotion(p);
        return ApiResponse.ok(p);
    }
    @DeleteMapping("/admin/promotions/{id}")
    @RequiresPermissions("promotion:manage")
    public ApiResponse<?> deletePromotion(@PathVariable Long id) {
        mapper.deletePromotion(id);
        return ApiResponse.ok(null);
    }
}
