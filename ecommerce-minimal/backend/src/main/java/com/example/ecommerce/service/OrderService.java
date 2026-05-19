package com.example.ecommerce.service;

import com.example.ecommerce.mapper.CartMapper;
import com.example.ecommerce.mapper.AddressMapper;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.mapper.MarketingMapper;
import com.example.ecommerce.model.CartItemView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderService {
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final AddressMapper addressMapper;
    private final MarketingMapper marketingMapper;

    public OrderService(CartMapper cartMapper, ProductMapper productMapper, OrderMapper orderMapper, AddressMapper addressMapper, MarketingMapper marketingMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.addressMapper = addressMapper;
        this.marketingMapper = marketingMapper;
    }

    @Transactional
    public String createOrder(Long userId, Long addressId, java.util.List<Long> productIds, Long couponId, String paymentMethod) {
        List<CartItemView> items = cartMapper.findByUserId(userId).stream()
                .filter(i -> productIds == null || productIds.isEmpty() || productIds.contains(i.getProductId()))
                .toList();
        if (items.isEmpty()) throw new IllegalStateException("购物车为空");
        if (addressMapper.findById(addressId) == null) throw new IllegalStateException("收货地址不存在");
        BigDecimal subtotal = BigDecimal.ZERO;
        for(CartItemView item:items){
            var flash=marketingMapper.findActiveFlashSale(item.getProductId());
            if(flash!=null){
                if(marketingMapper.decreaseFlashSaleStock(item.getProductId(),item.getQuantity())==0) throw new IllegalStateException("秒杀库存不足：" + item.getProductName());
                item.setPrice(flash.getPromotionPrice());
                item.setSubtotal(flash.getPromotionPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            subtotal=subtotal.add(item.getSubtotal());
        }
        BigDecimal discount = BigDecimal.ZERO;
        if(couponId!=null){
            var coupon=marketingMapper.findUsableCoupon(userId,couponId);
            if(coupon==null) throw new IllegalStateException("优惠券不可用");
            if(subtotal.compareTo(coupon.getThresholdAmount())>=0) discount=coupon.getDiscountAmount();
        }
        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO);
        for (CartItemView item : items) {
            if (productMapper.decreaseStock(item.getProductId(), item.getQuantity()) == 0) {
                throw new IllegalStateException("库存不足：" + item.getProductName());
            }
        }
        String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        orderMapper.insertOrder(orderNo, userId, addressId, total, "CREATED", couponId, discount, paymentMethod, LocalDateTime.now());
        Long orderId = orderMapper.findIdByOrderNo(orderNo);
        for (CartItemView item : items) {
            orderMapper.insertOrderItem(orderId, item.getProductId(), item.getProductName(), item.getSpecText(),
                    item.getPrice(), item.getQuantity(), item.getSubtotal());
        }
        if(couponId!=null && discount.compareTo(BigDecimal.ZERO)>0) marketingMapper.useCoupon(userId,couponId);
        if(productIds == null || productIds.isEmpty()) cartMapper.clearByUserId(userId);
        else cartMapper.deleteSelected(userId, productIds);
        return orderNo;
    }
}
