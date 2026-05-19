package com.example.ecommerce.mapper;

import com.example.ecommerce.model.CartItemView;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CartMapper {
    int upsert(@Param("userId") Long userId, @Param("productId") Long productId, @Param("specText") String specText, @Param("quantity") Integer quantity);
    List<CartItemView> findByUserId(Long userId);
    int updateQuantity(@Param("userId") Long userId, @Param("productId") Long productId, @Param("quantity") Integer quantity);
    int deleteItem(@Param("userId") Long userId, @Param("productId") Long productId);
    int clearByUserId(Long userId);
    int deleteSelected(@Param("userId") Long userId, @Param("productIds") java.util.List<Long> productIds);
}
