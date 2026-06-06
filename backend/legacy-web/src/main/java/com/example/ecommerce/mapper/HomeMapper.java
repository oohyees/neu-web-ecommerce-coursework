package com.example.ecommerce.mapper;
import com.example.ecommerce.model.Banner;
import com.example.ecommerce.model.Product;
import java.util.List;
public interface HomeMapper {
    List<Banner> findBanners(String keyword);
    int insertBanner(Banner banner);
    int updateBanner(Banner banner);
    int deleteBanner(Long id);
    List<Product> findHotProducts();
    List<Product> findNewProducts();
    List<java.util.Map<String,Object>> findHotSearches();
    int trackSearchKeyword(String keyword);
}
