package com.example.ecommerce.mapper;

import com.example.ecommerce.model.ProductCategory;
import java.util.List;

public interface CategoryMapper {
    List<ProductCategory> findAll();
    int insert(ProductCategory category);
    int update(ProductCategory category);
    int delete(Long id);
}
