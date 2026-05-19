package com.example.ecommerce.controller;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.CategoryMapper;
import com.example.ecommerce.model.ProductCategory;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class CategoryController {
    private final CategoryMapper categoryMapper;
    public CategoryController(CategoryMapper categoryMapper) { this.categoryMapper = categoryMapper; }
    @GetMapping("/api/categories")
    public ApiResponse<?> list() { return ApiResponse.ok(categoryMapper.findAll()); }
    @PostMapping("/api/admin/categories")
    public ApiResponse<?> create(@RequestBody ProductCategory category) {
        categoryMapper.insert(category);
        return ApiResponse.ok(category);
    }
    @PutMapping("/api/admin/categories")
    public ApiResponse<?> update(@RequestBody ProductCategory category){categoryMapper.update(category);return ApiResponse.ok(category);}
    @DeleteMapping("/api/admin/categories/{id}")
    public ApiResponse<?> delete(@PathVariable Long id){categoryMapper.delete(id);return ApiResponse.ok(null);}
}
