package com.example.ecommerce.mapper;
import com.example.ecommerce.model.Product;
import org.apache.ibatis.annotations.Param;
import java.util.List;
public interface FavoriteMapper {
    int insert(@Param("userId") Long userId,@Param("productId") Long productId);
    int delete(@Param("userId") Long userId,@Param("productId") Long productId);
    int count(@Param("userId") Long userId,@Param("productId") Long productId);
    List<Product> findByUserId(Long userId);
}
