package com.example.ecommerce.mapper;

import com.example.ecommerce.model.ProductSku;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ProductSkuMapper {
    List<ProductSku> findByProductId(@Param("productId") Long productId);
    ProductSku findById(@Param("id") Long id);
    int insert(ProductSku sku);
    int update(ProductSku sku);
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
