package com.example.ecommerce.mapper;

import com.example.ecommerce.model.Product;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ProductMapper {
    List<Product> findAll(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, @Param("searchMode") String searchMode, @Param("minPrice") java.math.BigDecimal minPrice, @Param("maxPrice") java.math.BigDecimal maxPrice, @Param("sort") String sort, @Param("offset") Integer offset, @Param("size") Integer size);
    int countAll(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, @Param("searchMode") String searchMode, @Param("minPrice") java.math.BigDecimal minPrice, @Param("maxPrice") java.math.BigDecimal maxPrice);
    List<Product> findAllForAdmin(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, @Param("searchMode") String searchMode, @Param("minPrice") java.math.BigDecimal minPrice, @Param("maxPrice") java.math.BigDecimal maxPrice, @Param("sort") String sort, @Param("offset") Integer offset, @Param("size") Integer size);
    int countAllForAdmin(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, @Param("searchMode") String searchMode, @Param("minPrice") java.math.BigDecimal minPrice, @Param("maxPrice") java.math.BigDecimal maxPrice);
    Product findById(Long id);
    int insert(Product product);
    int update(Product product);
    int decreaseStock(Long id, Integer quantity);
    int increaseStock(@org.apache.ibatis.annotations.Param("id") Long id, @org.apache.ibatis.annotations.Param("quantity") Integer quantity);
    int delete(Long id);
    int forceDelete(Long id);
}
