package com.example.ecommerce.mapper;
import com.example.ecommerce.model.ProductReview;
import org.apache.ibatis.annotations.Param;
import java.util.List;
public interface ReviewMapper {
    List<ProductReview> findByProductId(Long productId);
    List<ProductReview> findByProductIdPage(@Param("productId") Long productId, @Param("offset") Integer offset, @Param("size") Integer size);
    int countByProductId(Long productId);
    List<ProductReview> findAll();
    List<ProductReview> findAllPage(@Param("offset") Integer offset, @Param("size") Integer size);
    int countAll();
    int insert(ProductReview review);
    int delete(Long id);
}
