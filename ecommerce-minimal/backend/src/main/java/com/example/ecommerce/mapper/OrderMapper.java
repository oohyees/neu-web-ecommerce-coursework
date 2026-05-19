package com.example.ecommerce.mapper;

import com.example.ecommerce.model.OrderView;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderMapper {
    int insertOrder(@Param("orderNo") String orderNo, @Param("userId") Long userId, @Param("addressId") Long addressId, @Param("totalAmount") BigDecimal totalAmount,
                    @Param("status") String status, @Param("couponId") Long couponId, @Param("discountAmount") BigDecimal discountAmount, @Param("paymentMethod") String paymentMethod, @Param("createdAt") LocalDateTime createdAt);
    Long findIdByOrderNo(String orderNo);
    int insertOrderItem(@Param("orderId") Long orderId, @Param("productId") Long productId, @Param("productName") String productName,
                        @Param("specText") String specText, @Param("unitPrice") BigDecimal unitPrice, @Param("quantity") Integer quantity, @Param("subtotal") BigDecimal subtotal);
    List<OrderView> findAll();
    List<OrderView> findByUserId(@Param("userId") Long userId, @Param("status") String status);
    List<OrderView> searchAdmin(@Param("keyword") String keyword, @Param("status") String status);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int updatePaymentStatus(@Param("id") Long id, @Param("paymentStatus") String paymentStatus);
    int updateLogisticsStatus(@Param("id") Long id, @Param("logisticsStatus") String logisticsStatus);
    int updateRefundStatus(@Param("id") Long id, @Param("refundStatus") String refundStatus);
    int insertLogistics(@Param("orderId") Long orderId, @Param("content") String content, @Param("createdAt") LocalDateTime createdAt);
    java.util.List<com.example.ecommerce.model.OrderLogistics> findLogistics(Long orderId);
    OrderView findById(Long id);
}
