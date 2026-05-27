package com.example.ecommerce.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final JdbcTemplate jdbc;
    private final CatalogClient catalogClient;

    public OrderController(JdbcTemplate jdbc, CatalogClient catalogClient) {
        this.jdbc = jdbc;
        this.catalogClient = catalogClient;
    }

    @GetMapping("/cart")
    public ApiResponse<?> cart(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(cartItems(userId));
    }

    @PostMapping("/cart/items")
    public ApiResponse<?> addCart(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        Long productId = longValue(body.get("productId"));
        int quantity = intValue(body.get("quantity"), 1);
        String specText = stringValue(body.get("specText"));
        jdbc.update("""
                insert into cart_item(user_id,product_id,spec_text,quantity) values(?,?,?,?)
                on duplicate key update quantity=quantity+values(quantity)
                """, userId, productId, specText, quantity);
        return ApiResponse.ok(null);
    }

    @PutMapping("/cart/items")
    public ApiResponse<?> updateCart(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        int updated;
        if (body.get("cartItemId") != null || body.get("id") != null) {
            updated = jdbc.update("update cart_item set quantity=? where id=? and user_id=?",
                    intValue(body.get("quantity"), 1), longValue(body.getOrDefault("cartItemId", body.get("id"))), userId);
        } else if (body.get("specText") == null) {
            updated = jdbc.update("update cart_item set quantity=? where user_id=? and product_id=?",
                    intValue(body.get("quantity"), 1), userId, longValue(body.get("productId")));
        } else {
            updated = jdbc.update("update cart_item set quantity=? where user_id=? and product_id=? and (spec_text <=> ?)",
                    intValue(body.get("quantity"), 1), userId, longValue(body.get("productId")), stringValue(body.get("specText")));
        }
        return updated == 0 ? ApiResponse.fail("购物车商品不存在") : ApiResponse.ok(null);
    }

    @DeleteMapping("/cart/items")
    public ApiResponse<?> deleteCart(@RequestHeader("X-User-Id") Long userId,
                                     @RequestParam(required = false) Long cartItemId,
                                     @RequestParam(required = false) Long productId,
                                     @RequestParam(required = false) String specText) {
        if (cartItemId != null) jdbc.update("delete from cart_item where id=? and user_id=?", cartItemId, userId);
        else jdbc.update("delete from cart_item where user_id=? and product_id=? and (spec_text <=> ?)", userId, productId, specText);
        return ApiResponse.ok(null);
    }

    @GetMapping("/addresses")
    public ApiResponse<?> addresses(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(jdbc.queryForList("""
                select id,user_id userId,receiver_name receiverName,phone,province,city,district,
                       detail_address detailAddress,is_default isDefault
                from user_address where user_id=? order by is_default desc,id
                """, userId));
    }

    @PostMapping("/addresses")
    public ApiResponse<?> createAddress(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        if (boolValue(body.get("isDefault"))) jdbc.update("update user_address set is_default=0 where user_id=?", userId);
        jdbc.update("""
                insert into user_address(user_id,receiver_name,phone,province,city,district,detail_address,is_default)
                values(?,?,?,?,?,?,?,?)
                """, userId, stringValue(body.get("receiverName")), stringValue(body.get("phone")),
                stringValue(body.get("province")), stringValue(body.get("city")), stringValue(body.get("district")),
                stringValue(body.get("detailAddress")), boolValue(body.get("isDefault")) ? 1 : 0);
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("""
                select id,user_id userId,receiver_name receiverName,phone,province,city,district,
                       detail_address detailAddress,is_default isDefault
                from user_address where id=?
                """, id));
    }

    @PutMapping("/addresses")
    public ApiResponse<?> updateAddress(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        Long id = longValue(body.get("id"));
        if (boolValue(body.get("isDefault"))) jdbc.update("update user_address set is_default=0 where user_id=?", userId);
        int updated = jdbc.update("""
                update user_address set receiver_name=?,phone=?,province=?,city=?,district=?,detail_address=?,is_default=?
                where id=? and user_id=?
                """, stringValue(body.get("receiverName")), stringValue(body.get("phone")),
                stringValue(body.get("province")), stringValue(body.get("city")), stringValue(body.get("district")),
                stringValue(body.get("detailAddress")), boolValue(body.get("isDefault")) ? 1 : 0, id, userId);
        if (updated == 0) return ApiResponse.fail("收货地址不存在");
        return ApiResponse.ok(jdbc.queryForMap("""
                select id,user_id userId,receiver_name receiverName,phone,province,city,district,
                       detail_address detailAddress,is_default isDefault
                from user_address where id=?
                """, id));
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<?> deleteAddress(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        jdbc.update("delete from user_address where id=? and user_id=?", id, userId);
        return ApiResponse.ok(null);
    }

    @PutMapping("/addresses/{id}/default")
    public ApiResponse<?> setDefaultAddress(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        if (!ownsAddress(userId, id)) return ApiResponse.fail("收货地址不存在");
        jdbc.update("update user_address set is_default=0 where user_id=?", userId);
        jdbc.update("update user_address set is_default=1 where id=? and user_id=?", id, userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/orders")
    @Transactional
    public ApiResponse<?> createOrder(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        Long addressId = longValue(body.get("addressId"));
        if (!ownsAddress(userId, addressId)) return ApiResponse.fail("收货地址不存在");
        List<Long> cartItemIds = longList(body.get("cartItemIds"));
        List<Long> productIds = longList(body.get("productIds"));
        List<Map<String, Object>> selected = cartItems(userId).stream()
                .filter(i -> !cartItemIds.isEmpty() ? cartItemIds.contains(longValue(i.get("id"))) : (productIds.isEmpty() || productIds.contains(longValue(i.get("productId")))))
                .toList();
        if (selected.isEmpty()) return ApiResponse.fail("购物车为空");

        BigDecimal total = BigDecimal.ZERO;
        String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        jdbc.update("""
                insert into orders(order_no,user_id,address_id,total_amount,status,payment_status,logistics_status,refund_status,payment_method,created_at)
                values(?,?,?,?,?,?,?,?,?,?)
                """, orderNo, userId, addressId, BigDecimal.ZERO, "CREATED", "UNPAID", "待支付", "NONE",
                stringValue(body.getOrDefault("paymentMethod", "MOCK_PAY")), LocalDateTime.now());
        Long orderId = jdbc.queryForObject("select id from orders where order_no=?", Long.class, orderNo);

        for (Map<String, Object> item : selected) {
            Long productId = longValue(item.get("productId"));
            int quantity = intValue(item.get("quantity"), 1);
            log.info("Feign call catalog-service productId={} action=order-view", productId);
            ApiResponse<CatalogClient.ProductOrderView> productResponse = catalogClient.orderView(productId);
            if (!productResponse.success() || productResponse.data() == null) throw new IllegalStateException("商品不存在：" + productId);
            CatalogClient.ProductOrderView product = productResponse.data();
            log.info("Feign call catalog-service productId={} action=deduct-stock quantity={}", productId, quantity);
            ApiResponse<Void> deductResponse = catalogClient.deductStock(productId, quantity);
            if (!deductResponse.success()) throw new IllegalStateException("库存不足：" + product.name());
            BigDecimal subtotal = product.price().multiply(BigDecimal.valueOf(quantity));
            total = total.add(subtotal);
            jdbc.update("""
                    insert into order_item(order_id,product_id,product_name,spec_text,unit_price,quantity,subtotal)
                    values(?,?,?,?,?,?,?)
                    """, orderId, productId, product.name(), stringValue(item.get("specText")), product.price(), quantity, subtotal);
        }

        jdbc.update("update orders set total_amount=? where id=?", total, orderId);
        if (!cartItemIds.isEmpty()) {
            String marks = "?,".repeat(cartItemIds.size());
            marks = marks.substring(0, marks.length() - 1);
            List<Object> args = new ArrayList<>();
            args.add(userId);
            args.addAll(cartItemIds);
            jdbc.update("delete from cart_item where user_id=? and id in (" + marks + ")", args.toArray());
        } else if (productIds.isEmpty()) {
            jdbc.update("delete from cart_item where user_id=?", userId);
        } else {
            String marks = "?,".repeat(productIds.size());
            marks = marks.substring(0, marks.length() - 1);
            List<Object> args = new ArrayList<>();
            args.add(userId);
            args.addAll(productIds);
            jdbc.update("delete from cart_item where user_id=? and product_id in (" + marks + ")", args.toArray());
        }
        jdbc.update("insert into order_logistics(order_id,content,created_at) values(?,?,?)", orderId, "订单已创建", LocalDateTime.now());
        return ApiResponse.ok(Map.of("id", orderId, "orderNo", orderNo));
    }

    @GetMapping("/orders")
    public ApiResponse<?> orders(@RequestHeader("X-User-Id") Long userId,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String paymentStatus) {
        List<Object> args = new ArrayList<>();
        args.add(userId);
        StringBuilder sql = new StringBuilder("select id,order_no orderNo,user_id userId,address_id addressId,total_amount totalAmount,status,payment_status paymentStatus,logistics_status logisticsStatus,refund_status refundStatus,coupon_id couponId,discount_amount discountAmount,payment_method paymentMethod,created_at createdAt from orders where user_id=?");
        if (status != null && !status.isBlank()) {
            sql.append(" and status=?");
            args.add(status);
        }
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            sql.append(" and payment_status=?");
            args.add(paymentStatus);
        }
        sql.append(" order by created_at desc,id desc");
        return ApiResponse.ok(jdbc.queryForList(sql.toString(), args.toArray()));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<?> detail(@RequestHeader("X-User-Id") Long userId, @RequestHeader("X-Role") String role, @PathVariable Long id) {
        var orders = jdbc.queryForList("select id,order_no orderNo,user_id userId,address_id addressId,total_amount totalAmount,status,payment_status paymentStatus,logistics_status logisticsStatus,refund_status refundStatus,coupon_id couponId,discount_amount discountAmount,payment_method paymentMethod,created_at createdAt from orders where id=?", id);
        if (orders.isEmpty()) return ApiResponse.fail("订单不存在");
        Map<String, Object> order = orders.get(0);
        if ("USER".equals(role) && !userId.equals(longValue(order.get("userId")))) return ApiResponse.fail("无权访问该订单");
        var items = jdbc.queryForList("select product_name productName,spec_text specText,unit_price unitPrice,quantity,subtotal from order_item where order_id=? order by id", id);
        var addresses = jdbc.queryForList("select province,city,district,detail_address detailAddress from user_address where id=?", longValue(order.get("addressId")));
        var logistics = jdbc.queryForList("select id,content,created_at createdAt from order_logistics where order_id=? order by created_at,id", id);
        Map<String,Object> data = new LinkedHashMap<>();
        data.put("order", order);
        data.put("items", items);
        data.put("address", addresses.isEmpty() ? null : addresses.get(0));
        data.put("logistics", logistics);
        return ApiResponse.ok(data);
    }

    @PutMapping("/orders/{id}/pay")
    public ApiResponse<?> pay(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        if (!ownsOrder(userId, id)) return ApiResponse.fail("无权操作该订单");
        jdbc.update("update orders set status='PAID',payment_status='PAID',logistics_status='待发货' where id=?", id);
        jdbc.update("insert into order_logistics(order_id,content,created_at) values(?,?,?)", id, "订单已支付", LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @PutMapping("/orders/{id}/pay-gateway")
    public ApiResponse<?> payGateway(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id, @RequestBody Map<String, String> body) {
        if (!ownsOrder(userId, id)) return ApiResponse.fail("无权操作该订单");
        jdbc.update("insert into order_logistics(order_id,content,created_at) values(?,?,?)", id, "通过" + body.getOrDefault("method", "MOCK_PAY") + "完成支付", LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @PutMapping("/orders/{id}/cancel")
    public ApiResponse<?> cancel(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        if (!ownsOrder(userId, id)) return ApiResponse.fail("无权操作该订单");
        jdbc.update("update orders set status='CANCELLED',logistics_status='已取消' where id=?", id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/orders/{id}/confirm")
    public ApiResponse<?> confirm(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        if (!ownsOrder(userId, id)) return ApiResponse.fail("无权操作该订单");
        jdbc.update("update orders set status='COMPLETED',logistics_status='已签收' where id=?", id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/orders/{id}/refund")
    public ApiResponse<?> refund(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        if (!ownsOrder(userId, id)) return ApiResponse.fail("无权操作该订单");
        jdbc.update("update orders set refund_status='REQUESTED' where id=?", id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/orders/{id}/logistics")
    public ApiResponse<?> logistics(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        if (!ownsOrder(userId, id)) return ApiResponse.fail("无权访问该订单");
        return ApiResponse.ok(jdbc.queryForList("select id,content,created_at createdAt from order_logistics where order_id=? order by created_at,id", id));
    }

    private List<Map<String, Object>> cartItems(Long userId) {
        return jdbc.queryForList("""
                select c.id,c.product_id productId,p.name productName,p.image_url imageUrl,c.spec_text specText,
                       p.price,c.quantity,p.price*c.quantity subtotal
                from cart_item c join product p on p.id=c.product_id
                where c.user_id=? order by c.id
                """, userId);
    }

    private boolean ownsAddress(Long userId, Long addressId) {
        Integer count = jdbc.queryForObject("select count(*) from user_address where id=? and user_id=?", Integer.class, addressId, userId);
        return count != null && count > 0;
    }

    private boolean ownsOrder(Long userId, Long orderId) {
        Integer count = jdbc.queryForObject("select count(*) from orders where id=? and user_id=?", Integer.class, orderId, userId);
        return count != null && count > 0;
    }

    private List<Long> longList(Object value) {
        if (!(value instanceof List<?> raw)) return List.of();
        return raw.stream().map(v -> Long.valueOf(String.valueOf(v))).toList();
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
        if (value == null) return false;
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number number) return number.intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
