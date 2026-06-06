package com.example.ecommerce.common;

import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.model.OrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Auto-cancels unpaid orders after 30 minutes and restores inventory.
 */
@Component
public class OrderScheduler {
    private static final Logger log = LoggerFactory.getLogger(OrderScheduler.class);
    private static final int EXPIRE_MINUTES = 30;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public OrderScheduler(OrderMapper orderMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
    }

    @Scheduled(fixedDelay = 60000) // Every 60 seconds
    public void cancelExpiredOrders() {
        List<OrderView> expired = orderMapper.findExpiredUnpaid(EXPIRE_MINUTES);
        if (expired.isEmpty()) return;
        for (OrderView order : expired) {
            try {
                orderMapper.updateStatus(order.getId(), "CANCELLED");
                orderMapper.insertLogistics(order.getId(), "订单超时未支付，系统自动取消", java.time.LocalDateTime.now());
                // Restore stock for each item
                List<Map<String, Object>> items = orderMapper.findOrderItems(order.getId());
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        Long productId = Long.valueOf(String.valueOf(item.get("productId")));
                        int quantity = Integer.parseInt(String.valueOf(item.get("quantity")));
                        productMapper.increaseStock(productId, quantity);
                    }
                }
                log.info("Auto-cancelled expired order: {}", order.getOrderNo());
            } catch (Exception e) {
                log.error("Failed to auto-cancel order: {}", order.getOrderNo(), e);
            }
        }
    }
}
