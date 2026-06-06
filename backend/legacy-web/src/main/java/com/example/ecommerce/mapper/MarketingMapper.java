package com.example.ecommerce.mapper;
import com.example.ecommerce.model.Coupon;
import com.example.ecommerce.model.ProductSpec;
import com.example.ecommerce.model.Promotion;
import org.apache.ibatis.annotations.Param;
import java.util.List;
public interface MarketingMapper {
    List<ProductSpec> findSpecs(Long productId);
    List<Coupon> findCoupons();
    List<Coupon> findUserCoupons(Long userId);
    int claimCoupon(Long userId, Long couponId);
    Coupon findUsableCoupon(Long userId, Long couponId);
    int useCoupon(Long userId, Long couponId);
    // Admin coupon CRUD
    List<Coupon> findAllCoupons(@Param("keyword") String keyword);
    int insertCoupon(Coupon c);
    int updateCoupon(Coupon c);
    int deleteCoupon(Long id);
    List<Promotion> findPromotions();
    Promotion findActiveFlashSale(Long productId);
    int decreaseFlashSaleStock(Long productId, Integer quantity);
    List<Promotion> findAllPromotions(String keyword);
    int insertPromotion(Promotion p);
    int updatePromotion(Promotion p);
    int deletePromotion(Long id);
}
