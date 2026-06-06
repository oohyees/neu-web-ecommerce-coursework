package com.example.ecommerce.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "ecommerce-product-service")
public interface ProductClient {
    @GetMapping("/internal/products/{id}/order-view")
    ApiResponse<ProductOrderView> orderView(@PathVariable("id") Long id);

    @PostMapping("/internal/products/{id}/deduct-stock")
    ApiResponse<Void> deductStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);

    record ProductOrderView(Long id, String name, BigDecimal price, Integer stock) {}
}
