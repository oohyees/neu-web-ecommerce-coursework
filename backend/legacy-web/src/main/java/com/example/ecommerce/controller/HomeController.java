package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.HomeMapper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/home") @CrossOrigin
public class HomeController {
    private final HomeMapper homeMapper;
    public HomeController(HomeMapper homeMapper){this.homeMapper=homeMapper;}
    @GetMapping @Cacheable("home") public ApiResponse<?> index(){ return ApiResponse.ok(Map.of("banners",homeMapper.findBanners(null),"hotProducts",homeMapper.findHotProducts(),"newProducts",homeMapper.findNewProducts(),"hotSearches",homeMapper.findHotSearches())); }
    @GetMapping("/banners") public ApiResponse<?> banners(@RequestParam(required=false) String keyword){return ApiResponse.ok(homeMapper.findBanners(keyword));}
    @CacheEvict(value = "home", allEntries = true)
    @PostMapping("/banners") public ApiResponse<?> create(@RequestBody com.example.ecommerce.model.Banner banner){homeMapper.insertBanner(banner);return ApiResponse.ok(banner);}
    @CacheEvict(value = "home", allEntries = true)
    @PutMapping("/banners") public ApiResponse<?> update(@RequestBody com.example.ecommerce.model.Banner banner){homeMapper.updateBanner(banner);return ApiResponse.ok(banner);}
    @CacheEvict(value = "home", allEntries = true)
    @DeleteMapping("/banners/{id}") public ApiResponse<?> delete(@PathVariable Long id){homeMapper.deleteBanner(id);return ApiResponse.ok(null);}

    @GetMapping("/hot-searches")
    public ApiResponse<?> hotSearches() { return ApiResponse.ok(homeMapper.findHotSearches()); }

    @PostMapping("/search/track")
    public ApiResponse<?> trackSearch(@RequestBody Map<String,String> body) {
        String keyword = body.get("keyword");
        if (keyword != null && !keyword.isBlank()) homeMapper.trackSearchKeyword(keyword.trim());
        return ApiResponse.ok(null);
    }
}
