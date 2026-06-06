package com.example.ecommerce.mapper;

import com.example.ecommerce.model.ProductImage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ProductImageMapper {
    List<ProductImage> findByProductId(@Param("productId") Long productId);
    int insert(ProductImage image);
}
