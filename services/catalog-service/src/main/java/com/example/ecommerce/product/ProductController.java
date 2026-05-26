package com.example.ecommerce.product;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final JdbcTemplate jdbc;

    public ProductController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/products")
    public ApiResponse<?> list(@RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String searchMode,
                               @RequestParam(required = false) BigDecimal minPrice,
                               @RequestParam(required = false) BigDecimal maxPrice,
                               @RequestParam(defaultValue = "default") String sort,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "12") int size) {
        QueryParts query = productQuery(false, categoryId, keyword, searchMode, minPrice, maxPrice);
        String orderBy = switch (sort == null ? "default" : sort) {
            case "price_asc" -> " order by price asc";
            case "price_desc" -> " order by price desc";
            case "sales_desc" -> " order by sales desc";
            case "newest" -> " order by id desc";
            default -> " order by id desc";
        };
        int offset = Math.max(page - 1, 0) * size;
        List<Object> listArgs = new ArrayList<>(query.args());
        listArgs.add(size);
        listArgs.add(offset);
        var items = jdbc.queryForList(productSelect() + query.where() + orderBy + " limit ? offset ?", listArgs.toArray());
        Integer total = jdbc.queryForObject("select count(*) from product" + query.where(), Integer.class, query.args().toArray());
        return ApiResponse.ok(Map.of("items", items, "total", total == null ? 0 : total));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        var items = jdbc.queryForList(productSelect() + " where id=? and is_on_sale=1", id);
        return items.isEmpty() ? ApiResponse.fail("商品不存在") : ApiResponse.ok(items.get(0));
    }

    @GetMapping("/products/admin/all")
    public ApiResponse<?> adminList(@RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String searchMode,
                                    @RequestParam(required = false) BigDecimal minPrice,
                                    @RequestParam(required = false) BigDecimal maxPrice,
                                    @RequestParam(defaultValue = "default") String sort,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        QueryParts query = productQuery(true, categoryId, keyword, searchMode, minPrice, maxPrice);
        int offset = Math.max(page - 1, 0) * size;
        var args = new ArrayList<>(query.args());
        args.add(size);
        args.add(offset);
        var items = jdbc.queryForList(productSelect() + query.where() + " order by id desc limit ? offset ?", args.toArray());
        Integer total = jdbc.queryForObject("select count(*) from product" + query.where(), Integer.class, query.args().toArray());
        return ApiResponse.ok(Map.of("items", items, "total", total == null ? 0 : total));
    }

    @PostMapping("/products/admin")
    public ApiResponse<?> createProduct(@RequestBody Map<String, Object> body) {
        jdbc.update("""
                insert into product(category_id,name,price,stock,sales,is_on_sale,image_url,detail_html,params_text)
                values(?,?,?,?,?,?,?,?,?)
                """, longValue(body.get("categoryId")), stringValue(body.get("name")), new BigDecimal(String.valueOf(body.getOrDefault("price", "0"))),
                intValue(body.get("stock"), 0), intValue(body.get("sales"), 0), boolValue(body.get("isOnSale")) ? 1 : 0,
                stringValue(body.get("imageUrl")), stringValue(body.get("detailHtml")), stringValue(body.get("paramsText")));
        return ApiResponse.ok(null);
    }

    @PutMapping("/products/admin")
    public ApiResponse<?> updateProduct(@RequestBody Map<String, Object> body) {
        jdbc.update("""
                update product set category_id=?,name=?,price=?,stock=?,sales=?,is_on_sale=?,image_url=?,detail_html=?,params_text=?
                where id=?
                """, longValue(body.get("categoryId")), stringValue(body.get("name")), new BigDecimal(String.valueOf(body.getOrDefault("price", "0"))),
                intValue(body.get("stock"), 0), intValue(body.get("sales"), 0), boolValue(body.get("isOnSale")) ? 1 : 0,
                stringValue(body.get("imageUrl")), stringValue(body.get("detailHtml")), stringValue(body.get("paramsText")), longValue(body.get("id")));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/products/admin/{id}")
    public ApiResponse<?> deleteProduct(@PathVariable Long id) {
        jdbc.update("update product set is_on_sale=0,stock=0 where id=?", id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/products/admin/export")
    public void exportProducts(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=products.csv");
        response.getWriter().println("id,categoryId,name,price,stock,sales");
        for (Map<String, Object> row : jdbc.queryForList(productSelect() + " order by id desc")) {
            response.getWriter().printf("%s,%s,%s,%s,%s,%s%n", row.get("id"), row.get("categoryId"), row.get("name"), row.get("price"), row.get("stock"), row.get("sales"));
        }
    }

    @PostMapping("/products/admin/import")
    public ApiResponse<?> importProducts(@RequestParam MultipartFile file) {
        return ApiResponse.ok(Map.of("count", 0, "message", "微服务演示版已接收文件：" + (file == null ? "" : file.getOriginalFilename())));
    }

    @GetMapping("/categories")
    public ApiResponse<?> categories() {
        return ApiResponse.ok(jdbc.queryForList("select id,parent_id parentId,name,sort_order sortOrder from product_category order by sort_order,id"));
    }

    @GetMapping("/home")
    public ApiResponse<?> home() {
        return ApiResponse.ok(Map.of(
                "banners", jdbc.queryForList("select id,title,image_url imageUrl,link_url linkUrl,sort_order sortOrder from banner order by sort_order,id"),
                "hotProducts", jdbc.queryForList(productSelect() + " where is_on_sale=1 order by sales desc,id desc limit 6"),
                "newProducts", jdbc.queryForList(productSelect() + " where is_on_sale=1 order by id desc limit 6")
        ));
    }

    @GetMapping("/home/banners")
    public ApiResponse<?> banners() {
        return ApiResponse.ok(jdbc.queryForList("select id,title,image_url imageUrl,link_url linkUrl,sort_order sortOrder from banner order by sort_order,id"));
    }

    @GetMapping("/announcements")
    public ApiResponse<?> announcements() {
        return ApiResponse.ok(jdbc.queryForList("select id,title,content,created_at createdAt from announcement order by created_at desc,id desc"));
    }

    @GetMapping("/activity-notices")
    public ApiResponse<?> activityNotices() {
        return ApiResponse.ok(jdbc.queryForList("select id,title,content,enabled,created_at createdAt from activity_notice where enabled=1 order by id desc"));
    }

    @GetMapping("/marketing/specs/{productId}")
    public ApiResponse<?> specs(@PathVariable Long productId) {
        return ApiResponse.ok(jdbc.queryForList("select id,product_id productId,spec_name specName,spec_value specValue from product_spec where product_id=?", productId));
    }

    @GetMapping("/marketing/promotions")
    public ApiResponse<?> promotions() {
        return ApiResponse.ok(jdbc.queryForList("""
                select id,product_id productId,title,promotion_type promotionType,promotion_price promotionPrice,
                       promotion_stock promotionStock,start_at startAt,end_at endAt,enabled
                from promotion where enabled=1 and now() between start_at and end_at order by id desc
                """));
    }

    @GetMapping("/marketing/coupons")
    public ApiResponse<?> coupons() {
        return ApiResponse.ok(jdbc.queryForList("select id,name,threshold_amount thresholdAmount,discount_amount discountAmount,enabled from coupon where enabled=1 order by threshold_amount"));
    }

    @GetMapping("/marketing/coupons/user/{ignoredUserId}")
    public ApiResponse<?> userCoupons(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(jdbc.queryForList("""
                select c.id,c.name,c.threshold_amount thresholdAmount,c.discount_amount discountAmount,c.enabled,uc.status
                from user_coupon uc join coupon c on c.id=uc.coupon_id
                where uc.user_id=? order by c.threshold_amount
                """, userId));
    }

    @PostMapping("/marketing/coupons/{couponId}/claim")
    public ApiResponse<?> claimCoupon(@RequestHeader("X-User-Id") Long userId, @PathVariable Long couponId) {
        jdbc.update("insert ignore into user_coupon(user_id,coupon_id,status,claimed_at) values(?,?, 'UNUSED', now())", userId, couponId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/reviews")
    public ApiResponse<?> reviews(@RequestParam Long productId) {
        return ApiResponse.ok(jdbc.queryForList("select id,user_id userId,product_id productId,rating,content,image_url imageUrl,created_at createdAt from product_review where product_id=? order by created_at desc,id desc", productId));
    }

    @PostMapping("/reviews")
    public ApiResponse<?> createReview(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        jdbc.update("insert into product_review(user_id,product_id,rating,content,image_url,created_at) values(?,?,?,?,?,?)",
                userId, longValue(body.get("productId")), intValue(body.get("rating"), 5),
                String.valueOf(body.getOrDefault("content", "")), stringValue(body.get("imageUrl")), LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @GetMapping("/favorites/{productId}/status")
    public ApiResponse<?> favoriteStatus(@RequestHeader("X-User-Id") Long userId, @PathVariable Long productId) {
        Integer count = jdbc.queryForObject("select count(*) from product_favorite where user_id=? and product_id=?", Integer.class, userId, productId);
        return ApiResponse.ok(count != null && count > 0);
    }

    @PostMapping("/favorites/{productId}")
    public ApiResponse<?> addFavorite(@RequestHeader("X-User-Id") Long userId, @PathVariable Long productId) {
        jdbc.update("insert ignore into product_favorite(user_id,product_id) values(?,?)", userId, productId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/favorites/{productId}")
    public ApiResponse<?> removeFavorite(@RequestHeader("X-User-Id") Long userId, @PathVariable Long productId) {
        jdbc.update("delete from product_favorite where user_id=? and product_id=?", userId, productId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/favorites")
    public ApiResponse<?> favorites(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(jdbc.queryForList(productSelect("p") + " from product_favorite f join product p on p.id=f.product_id where f.user_id=? order by f.id desc", userId));
    }

    @PostMapping("/files/upload")
    public ApiResponse<?> upload(@RequestParam MultipartFile file) {
        String name = file == null || file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        return ApiResponse.ok("https://dummyimage.com/320x240/e5e7eb/374151&text=" + name.replaceAll("[^a-zA-Z0-9._-]", "_"));
    }

    @GetMapping("/internal/catalog/products/{id}/order-view")
    public ApiResponse<?> orderView(@PathVariable Long id) {
        log.info("Internal order query catalog-service productId={}", id);
        var items = jdbc.queryForList("select id,name,price,stock from product where id=? and is_on_sale=1", id);
        return items.isEmpty() ? ApiResponse.fail("商品不存在") : ApiResponse.ok(items.get(0));
    }

    @PostMapping("/internal/catalog/products/{id}/deduct-stock")
    public ApiResponse<?> deductStock(@PathVariable Long id, @RequestParam int quantity) {
        log.info("Internal order deduct stock catalog-service productId={} quantity={}", id, quantity);
        int updated = jdbc.update("update product set stock=stock-?, sales=sales+? where id=? and is_on_sale=1 and stock>=?", quantity, quantity, id, quantity);
        return updated == 0 ? ApiResponse.fail("库存不足") : ApiResponse.ok(null);
    }

    private String productSelect() {
        return productSelect(null) + " from product";
    }

    private String productSelect(String alias) {
        String p = alias == null || alias.isBlank() ? "" : alias + ".";
        return "select " + p + "id," + p + "category_id categoryId," + p + "name," + p + "price," + p + "stock," + p + "sales," +
                p + "is_on_sale isOnSale," + p + "image_url imageUrl," + p + "detail_html detailHtml," + p + "params_text paramsText";
    }

    private QueryParts productQuery(boolean includeDeleted, Long categoryId, String keyword, String searchMode, BigDecimal minPrice, BigDecimal maxPrice) {
        List<String> clauses = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        if (!includeDeleted) clauses.add("is_on_sale=1");
        if (categoryId != null) {
            clauses.add("category_id=?");
            args.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            clauses.add("exact".equals(searchMode) ? "name=?" : "name like ?");
            args.add("exact".equals(searchMode) ? keyword : "%" + keyword + "%");
        }
        if (minPrice != null) {
            clauses.add("price>=?");
            args.add(minPrice);
        }
        if (maxPrice != null) {
            clauses.add("price<=?");
            args.add(maxPrice);
        }
        return new QueryParts(clauses.isEmpty() ? "" : " where " + String.join(" and ", clauses), args);
    }

    private Long longValue(Object value) {
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private int intValue(Object value, int fallback) {
        return value == null ? fallback : Integer.parseInt(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean boolValue(Object value) {
        if (value == null) return true;
        if (value instanceof Boolean bool) return bool;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private record QueryParts(String where, List<Object> args) {}
}
