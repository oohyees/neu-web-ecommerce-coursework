package com.example.ecommerce.product;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private static final long MAX_UPLOAD_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
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
            case "price_asc" -> " order by (image_url like '%dummyjson%') desc, price asc";
            case "price_desc" -> " order by (image_url like '%dummyjson%') desc, price desc";
            case "sales_desc" -> " order by (image_url like '%dummyjson%') desc, sales desc";
            case "newest" -> " order by (image_url like '%dummyjson%') desc, id desc";
            default -> " order by (image_url like '%dummyjson%') desc, id desc";
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
        if (items.isEmpty()) return ApiResponse.fail("商品不存在");
        return ApiResponse.ok(Map.of(
                "product", items.get(0),
                "skus", jdbc.queryForList("""
                        select id,product_id productId,sku_code skuCode,color,size,price,stock,image
                        from product_sku where product_id=? and deleted=0 order by id
                        """, id),
                "images", jdbc.queryForList("""
                        select id,product_id productId,url,sort_order sortOrder
                        from product_image where product_id=? and deleted=0 order by sort_order,id
                        """, id),
                "specs", jdbc.queryForList("""
                        select id,product_id productId,spec_name specName,spec_value specValue
                        from product_spec where product_id=? order by id
                        """, id)
        ));
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
                insert into product(category_id,name,subtitle,price,original_price,stock,sales,is_on_sale,image_url,detail_html,params_text)
                values(?,?,?,?,?,?,?,?,?,?,?)
                """, longValue(body.get("categoryId")), stringValue(body.get("name")), stringValue(body.get("subtitle")),
                new BigDecimal(String.valueOf(body.getOrDefault("price", "0"))), decimalValue(body.get("originalPrice")),
                intValue(body.get("stock"), 0), intValue(body.get("sales"), 0), boolValue(body.get("isOnSale")) ? 1 : 0,
                stringValue(body.get("imageUrl")), stringValue(body.get("detailHtml")), stringValue(body.get("paramsText")));
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap(productSelect() + " where id=?", id));
    }

    @PutMapping("/products/admin")
    public ApiResponse<?> updateProduct(@RequestBody Map<String, Object> body) {
        jdbc.update("""
                update product set category_id=?,name=?,subtitle=?,price=?,original_price=?,stock=?,sales=?,is_on_sale=?,image_url=?,detail_html=?,params_text=?
                where id=?
                """, longValue(body.get("categoryId")), stringValue(body.get("name")), stringValue(body.get("subtitle")),
                new BigDecimal(String.valueOf(body.getOrDefault("price", "0"))), decimalValue(body.get("originalPrice")),
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
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=products.xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("products");
            Row head = sheet.createRow(0);
            String[] headers = {"id", "categoryId", "name", "price", "stock", "sales"};
            for (int i = 0; i < headers.length; i++) head.createCell(i).setCellValue(headers[i]);
            int r = 1;
            for (Map<String, Object> item : jdbc.queryForList(productSelect() + " order by id desc")) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(String.valueOf(item.get("id")));
                row.createCell(1).setCellValue(String.valueOf(item.get("categoryId")));
                row.createCell(2).setCellValue(String.valueOf(item.get("name")));
                row.createCell(3).setCellValue(String.valueOf(item.get("price")));
                row.createCell(4).setCellValue(String.valueOf(item.get("stock")));
                row.createCell(5).setCellValue(String.valueOf(item.get("sales")));
            }
            wb.write(response.getOutputStream());
        }
    }

    @PostMapping("/products/admin/import")
    public ApiResponse<?> importProducts(@RequestParam MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return ApiResponse.fail("文件不能为空");
        int count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    if (line.toLowerCase(Locale.ROOT).contains("category")) continue;
                }
                String[] columns = line.split(",", -1);
                if (columns.length < 4) continue;
                jdbc.update("""
                        insert into product(category_id,name,price,stock,sales,is_on_sale,image_url,detail_html,params_text)
                        values(?,?,?,?,?,?,?,?,?)
                        """, Long.valueOf(columns[0].trim()), columns[1].trim(), new BigDecimal(columns[2].trim()),
                        Integer.valueOf(columns[3].trim()), 0, 1,
                        columns.length > 4 ? columns[4].trim() : "",
                        columns.length > 5 ? columns[5].trim() : "",
                        columns.length > 6 ? columns[6].trim() : "");
                count++;
            }
        }
        return ApiResponse.ok(Map.of("count", count));
    }

    @GetMapping("/categories")
    public ApiResponse<?> categories() {
        return ApiResponse.ok(jdbc.queryForList("select id,parent_id parentId,name,sort_order sortOrder from product_category order by sort_order,id"));
    }

    @GetMapping("/home")
    public ApiResponse<?> home() {
        var promotions = jdbc.queryForList("""
                select p.id,p.title,p.promotion_type promotionType,p.promotion_price promotionPrice,
                       p.promotion_stock promotionStock,p.start_at startAt,p.end_at endAt,
                       pr.id productId,pr.name productName,pr.image_url imageUrl,pr.price originalPrice
                from promotion p join product pr on pr.id=p.product_id
                where p.enabled=1 and now() between p.start_at and p.end_at
                order by p.id desc
                """);
        var coupons = jdbc.queryForList("select id,name,threshold_amount thresholdAmount,discount_amount discountAmount from coupon where enabled=1 order by threshold_amount");
        var reviews = jdbc.queryForList("""
                select r.id,r.product_id productId,r.rating,r.content,r.image_url imageUrl,r.created_at createdAt,
                       p.name productName,p.image_url productImage
                from product_review r join product p on p.id=r.product_id
                order by r.created_at desc limit 12
                """);
        // Banner with related products for composite display
        var bannerRows = jdbc.queryForList("select id,title,image_url imageUrl,link_url linkUrl,sort_order sortOrder from banner order by sort_order,id");
        for (var banner : bannerRows) {
            String linkUrl = (String) banner.get("linkUrl");
            Long productId = null;
            if (linkUrl != null && linkUrl.startsWith("/products/")) {
                try { productId = Long.parseLong(linkUrl.replace("/products/", "")); } catch (NumberFormatException ignored) {}
            }
            if (productId != null) {
                // Get 2 sibling products from the same category
                var related = jdbc.queryForList(
                    productSelect() + " where is_on_sale=1 and category_id=(select category_id from product where id=?) and id!=? order by sales desc limit 2",
                    productId, productId);
                banner.put("relatedProducts", related);
            } else {
                banner.put("relatedProducts", List.of());
            }
        }
        return ApiResponse.ok(Map.of(
                "banners", bannerRows,
                "hotProducts", jdbc.queryForList(productSelect() + " where is_on_sale=1 order by sales desc,id desc limit 12"),
                "newProducts", jdbc.queryForList(productSelect() + " where is_on_sale=1 order by (image_url like '%dummyjson%') desc, id desc limit 12"),
                "promotions", promotions,
                "coupons", coupons,
                "reviews", reviews
        ));
    }

    @GetMapping("/home/banners")
    public ApiResponse<?> banners(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ApiResponse.ok(jdbc.queryForList("select id,title,image_url imageUrl,link_url linkUrl,sort_order sortOrder from banner order by sort_order,id"));
        }
        return ApiResponse.ok(jdbc.queryForList("""
                select id,title,image_url imageUrl,link_url linkUrl,sort_order sortOrder from banner
                where title like ? order by sort_order,id
                """, "%" + keyword + "%"));
    }

    @PostMapping("/home/banners")
    public ApiResponse<?> createBanner(@RequestBody Map<String, Object> body) {
        jdbc.update("insert into banner(title,image_url,link_url,sort_order) values(?,?,?,?)",
                stringValue(body.get("title")), stringValue(body.get("imageUrl")), stringValue(body.get("linkUrl")), intValue(body.get("sortOrder"), 0));
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("select id,title,image_url imageUrl,link_url linkUrl,sort_order sortOrder from banner where id=?", id));
    }

    @PutMapping("/home/banners")
    public ApiResponse<?> updateBanner(@RequestBody Map<String, Object> body) {
        jdbc.update("update banner set title=?,image_url=?,link_url=?,sort_order=? where id=?",
                stringValue(body.get("title")), stringValue(body.get("imageUrl")), stringValue(body.get("linkUrl")), intValue(body.get("sortOrder"), 0), longValue(body.get("id")));
        return ApiResponse.ok(body);
    }

    @DeleteMapping("/home/banners/{id}")
    public ApiResponse<?> deleteBanner(@PathVariable Long id) {
        jdbc.update("delete from banner where id=?", id);
        return ApiResponse.ok(null);
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
                select promotion.id,promotion.product_id productId,promotion.title,promotion.promotion_type promotionType,
                       promotion.promotion_price promotionPrice,promotion.promotion_stock promotionStock,
                       promotion.start_at startAt,promotion.end_at endAt,promotion.enabled,
                       pr.name productName,pr.image_url imageUrl,pr.price originalPrice
                from promotion join product pr on pr.id=promotion.product_id
                where promotion.enabled=1 and now() between promotion.start_at and promotion.end_at
                order by promotion.id desc
                """));
    }

    @GetMapping("/marketing/admin/promotions")
    public ApiResponse<?> adminPromotions(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ApiResponse.ok(jdbc.queryForList("""
                    select id,product_id productId,title,promotion_type promotionType,promotion_price promotionPrice,
                           promotion_stock promotionStock,start_at startAt,end_at endAt,enabled
                    from promotion order by id desc
                    """));
        }
        return ApiResponse.ok(jdbc.queryForList("""
                select id,product_id productId,title,promotion_type promotionType,promotion_price promotionPrice,
                       promotion_stock promotionStock,start_at startAt,end_at endAt,enabled
                from promotion where title like ? order by id desc
                """, "%" + keyword + "%"));
    }

    @PostMapping("/marketing/admin/promotions")
    public ApiResponse<?> createPromotion(@RequestBody Map<String, Object> body) {
        jdbc.update("""
                insert into promotion(product_id,title,promotion_type,promotion_price,promotion_stock,start_at,end_at,enabled)
                values(?,?,?,?,?,?,?,?)
                """, longValue(body.get("productId")), stringValue(body.get("title")), stringValue(body.get("promotionType")),
                decimalValue(body.get("promotionPrice")), intValue(body.get("promotionStock"), 0),
                stringValue(body.get("startAt")), stringValue(body.get("endAt")), boolValue(body.get("enabled")) ? 1 : 0);
        return ApiResponse.ok(null);
    }

    @PutMapping("/marketing/admin/promotions")
    public ApiResponse<?> updatePromotion(@RequestBody Map<String, Object> body) {
        jdbc.update("""
                update promotion set product_id=?,title=?,promotion_type=?,promotion_price=?,promotion_stock=?,start_at=?,end_at=?,enabled=?
                where id=?
                """, longValue(body.get("productId")), stringValue(body.get("title")), stringValue(body.get("promotionType")),
                decimalValue(body.get("promotionPrice")), intValue(body.get("promotionStock"), 0),
                stringValue(body.get("startAt")), stringValue(body.get("endAt")), boolValue(body.get("enabled")) ? 1 : 0, longValue(body.get("id")));
        return ApiResponse.ok(body);
    }

    @DeleteMapping("/marketing/admin/promotions/{id}")
    public ApiResponse<?> deletePromotion(@PathVariable Long id) {
        jdbc.update("delete from promotion where id=?", id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/marketing/coupons")
    public ApiResponse<?> coupons() {
        return ApiResponse.ok(jdbc.queryForList("select id,name,threshold_amount thresholdAmount,discount_amount discountAmount,enabled from coupon where enabled=1 order by threshold_amount"));
    }

    @GetMapping("/marketing/admin/coupons")
    public ApiResponse<?> adminCoupons(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ApiResponse.ok(jdbc.queryForList("""
                    select id,name,threshold_amount thresholdAmount,discount_amount discountAmount,enabled
                    from coupon order by threshold_amount,id
                    """));
        }
        return ApiResponse.ok(jdbc.queryForList("""
                select id,name,threshold_amount thresholdAmount,discount_amount discountAmount,enabled
                from coupon where name like ? order by threshold_amount,id
                """, "%" + keyword + "%"));
    }

    @PostMapping("/marketing/admin/coupons")
    public ApiResponse<?> createCoupon(@RequestBody Map<String, Object> body) {
        jdbc.update("""
                insert into coupon(name,threshold_amount,discount_amount,enabled)
                values(?,?,?,?)
                """, stringValue(body.get("name")), decimalValue(body.get("thresholdAmount")),
                decimalValue(body.get("discountAmount")), boolValue(body.get("enabled")) ? 1 : 0);
        return ApiResponse.ok(null);
    }

    @PutMapping("/marketing/admin/coupons")
    public ApiResponse<?> updateCoupon(@RequestBody Map<String, Object> body) {
        jdbc.update("""
                update coupon set name=?,threshold_amount=?,discount_amount=?,enabled=?
                where id=?
                """, stringValue(body.get("name")), decimalValue(body.get("thresholdAmount")),
                decimalValue(body.get("discountAmount")), boolValue(body.get("enabled")) ? 1 : 0, longValue(body.get("id")));
        return ApiResponse.ok(body);
    }

    @DeleteMapping("/marketing/admin/coupons/{id}")
    public ApiResponse<?> deleteCoupon(@PathVariable Long id) {
        jdbc.update("delete from coupon where id=?", id);
        return ApiResponse.ok(null);
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
    public ApiResponse<?> reviews(@RequestParam Long productId,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ApiResponse.ok(jdbc.queryForList("""
                    select id,user_id userId,product_id productId,rating,content,image_url imageUrl,created_at createdAt
                    from product_review where product_id=? order by created_at desc,id desc
                    """, productId));
        }
        int safePage = Math.max(page == null ? 1 : page, 1);
        int safeSize = Math.min(Math.max(size == null ? 10 : size, 1), 100);
        Integer total = jdbc.queryForObject("select count(*) from product_review where product_id=?", Integer.class, productId);
        var items = jdbc.queryForList("""
                select id,user_id userId,product_id productId,rating,content,image_url imageUrl,created_at createdAt
                from product_review where product_id=? order by created_at desc,id desc limit ? offset ?
                """, productId, safeSize, (safePage - 1) * safeSize);
        return ApiResponse.ok(Map.of("items", items, "total", total == null ? 0 : total));
    }

    @PostMapping("/reviews")
    public ApiResponse<?> createReview(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        jdbc.update("insert into product_review(user_id,product_id,rating,content,image_url,created_at) values(?,?,?,?,?,?)",
                userId, longValue(body.get("productId")), intValue(body.get("rating"), 5),
                String.valueOf(body.getOrDefault("content", "")), stringValue(body.get("imageUrl")), LocalDateTime.now());
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("""
                select id,user_id userId,product_id productId,rating,content,image_url imageUrl,created_at createdAt
                from product_review where id=?
                """, id));
    }

    @GetMapping("/reviews/admin/all")
    public ApiResponse<?> allReviews(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Integer total = jdbc.queryForObject("select count(*) from product_review", Integer.class);
        var items = jdbc.queryForList("""
                select id,user_id userId,product_id productId,rating,content,image_url imageUrl,created_at createdAt
                from product_review order by created_at desc,id desc limit ? offset ?
                """, size, Math.max(0, (page - 1)) * size);
        return ApiResponse.ok(Map.of("items", items, "total", total == null ? 0 : total));
    }

    @DeleteMapping("/reviews/admin/{id}")
    public ApiResponse<?> deleteReview(@PathVariable Long id) {
        jdbc.update("delete from product_review where id=?", id);
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
    public ApiResponse<?> upload(@RequestParam MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return ApiResponse.fail("文件不能为空");
        if (file.getSize() > MAX_UPLOAD_SIZE) return ApiResponse.fail("文件不能超过 5MB");
        String original = file.getOriginalFilename() == null ? "" : Paths.get(file.getOriginalFilename()).getFileName().toString();
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 && dot < original.length() - 1 ? original.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(ext) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ApiResponse.fail("仅支持 jpg、jpeg、png、gif、webp 图片");
        }
        Path dir = Paths.get("uploads");
        Files.createDirectories(dir);
        String name = UUID.randomUUID() + "." + ext;
        Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return ApiResponse.ok("/uploads/" + name);
    }

    @GetMapping("/internal/products/{id}/order-view")
    public ApiResponse<?> orderView(@PathVariable Long id) {
        log.info("Internal order query product-service productId={}", id);
        var items = jdbc.queryForList("select id,name,price,stock from product where id=? and is_on_sale=1", id);
        return items.isEmpty() ? ApiResponse.fail("商品不存在") : ApiResponse.ok(items.get(0));
    }

    @PostMapping("/internal/products/{id}/deduct-stock")
    public ApiResponse<?> deductStock(@PathVariable Long id, @RequestParam int quantity) {
        log.info("Internal order deduct stock product-service productId={} quantity={}", id, quantity);
        int updated = jdbc.update("update product set stock=stock-?, sales=sales+? where id=? and is_on_sale=1 and stock>=?", quantity, quantity, id, quantity);
        return updated == 0 ? ApiResponse.fail("库存不足") : ApiResponse.ok(null);
    }

    private String productSelect() {
        return productSelect(null) + " from product";
    }

    private String productSelect(String alias) {
        String p = alias == null || alias.isBlank() ? "" : alias + ".";
        return "select " + p + "id," + p + "category_id categoryId," + p + "name," + p + "subtitle," + p + "price," +
                p + "original_price originalPrice," + p + "stock," + p + "sales," +
                p + "is_on_sale isOnSale," + p + "image_url imageUrl," + p + "detail_html detailHtml," + p + "params_text paramsText";
    }

    private QueryParts productQuery(boolean includeDeleted, Long categoryId, String keyword, String searchMode, BigDecimal minPrice, BigDecimal maxPrice) {
        List<String> clauses = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        if (!includeDeleted) clauses.add("is_on_sale=1");
        if (categoryId != null) {
            List<Long> catIds = new ArrayList<>();
            catIds.add(categoryId);
            List<Map<String, Object>> children = jdbc.queryForList("select id from product_category where parent_id=?", categoryId);
            for (Map<String, Object> child : children) catIds.add(((Number) child.get("id")).longValue());
            if (catIds.size() == 1) {
                clauses.add("category_id=?");
                args.add(categoryId);
            } else {
                clauses.add("category_id in (" + String.join(",", catIds.stream().map(id -> "?").toList()) + ")");
                args.addAll(catIds);
            }
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

    private BigDecimal decimalValue(Object value) {
        return value == null || String.valueOf(value).isBlank() ? null : new BigDecimal(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean boolValue(Object value) {
        if (value == null) return true;
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number number) return number.intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private record QueryParts(String where, List<Object> args) {}
}
