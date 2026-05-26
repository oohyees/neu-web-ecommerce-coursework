package com.example.ecommerce.mapper;

import com.example.ecommerce.model.CartItemView;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CartMapper {
    int upsert(@Param("userId") Long userId, @Param("productId") Long productId, @Param("specText") String specText, @Param("quantity") Integer quantity);
    List<CartItemView> findByUserId(Long userId);
    int updateQuantity(@Param("userId") Long userId, @Param("cartItemId") Long cartItemId, @Param("productId") Long productId, @Param("specText") String specText, @Param("quantity") Integer quantity);
    int deleteItem(@Param("userId") Long userId, @Param("cartItemId") Long cartItemId, @Param("productId") Long productId, @Param("specText") String specText);
    int clearByUserId(Long userId);
    int deleteSelected(@Param("userId") Long userId, @Param("productIds") java.util.List<Long> productIds);
    int deleteSelectedItems(@Param("userId") Long userId, @Param("cartItemIds") java.util.List<Long> cartItemIds);
}
