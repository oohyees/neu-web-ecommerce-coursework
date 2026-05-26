package com.example.ecommerce.mapper;
import com.example.ecommerce.model.ProductReview;
import java.util.List;
public interface ReviewMapper {
    List<ProductReview> findByProductId(Long productId);
    List<ProductReview> findAll();
    int insert(ProductReview review);
    int delete(Long id);
}
