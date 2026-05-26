package com.example.ecommerce.admin;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final JdbcTemplate jdbc;

    public AdminController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard() {
        return ApiResponse.ok(Map.of(
                "userCount", value("select count(*) from user"),
                "orderCount", value("select count(*) from orders"),
                "salesAmount", jdbc.queryForObject("select coalesce(sum(total_amount),0) from orders where payment_status='PAID'", java.math.BigDecimal.class),
                "todaySalesAmount", jdbc.queryForObject("select coalesce(sum(total_amount),0) from orders where payment_status='PAID' and date(created_at)=curdate()", java.math.BigDecimal.class),
                "salesTrend", jdbc.queryForList("select date(created_at) day,coalesce(sum(total_amount),0) amount from orders where payment_status='PAID' group by date(created_at) order by day desc limit 7"),
                "orderStatus", jdbc.queryForList("select status,count(*) value from orders group by status"),
                "hotProducts", jdbc.queryForList("select name,sales from product order by sales desc,id desc limit 8"),
                "refundRequests", value("select count(*) from orders where refund_status='REQUESTED'"),
                "pendingReplies", value("select count(*) from feedback where status='PENDING'"),
                "pendingConsultations", value("select count(*) from customer_consultation where status='PENDING'")
        ));
    }

    @GetMapping("/users")
    public ApiResponse<?> users(@RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        String where = keyword == null || keyword.isBlank() ? "" : " where username like ? or nickname like ? or email like ? or phone like ?";
        Object[] args = keyword == null || keyword.isBlank() ? new Object[]{} :
                new Object[]{"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"};
        var listArgs = new ArrayList<>();
        java.util.Collections.addAll(listArgs, args);
        listArgs.add(size);
        listArgs.add((page - 1) * size);
        var items = jdbc.queryForList("select id,username,nickname,email,phone,enabled from user" + where + " order by id desc limit ? offset ?", listArgs.toArray());
        Integer total = jdbc.queryForObject("select count(*) from user" + where, Integer.class, args);
        return ApiResponse.ok(Map.of("items", items, "total", total == null ? 0 : total));
    }

    @GetMapping("/orders")
    public ApiResponse<?> orders(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        var args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where 1=1");
        if (keyword != null && !keyword.isBlank()) {
            where.append(" and (order_no like ? or cast(user_id as char) like ?)");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        if (status != null && !status.isBlank()) {
            where.append(" and status=?");
            args.add(status);
        }
        Integer total = jdbc.queryForObject("select count(*) from orders" + where, Integer.class, args.toArray());
        args.add(size);
        args.add((page - 1) * size);
        var items = jdbc.queryForList(orderSelect() + where + " order by created_at desc,id desc limit ? offset ?", args.toArray());
        return ApiResponse.ok(Map.of("items", items, "total", total == null ? 0 : total));
    }

    @PutMapping("/orders/{id}/ship")
    public ApiResponse<?> ship(@PathVariable Long id) {
        jdbc.update("update orders set status='SHIPPED',logistics_status='已发货' where id=?", id);
        jdbc.update("insert into order_logistics(order_id,content,created_at) values(?,?,?)", id, "商家已发货", LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @PutMapping("/orders/{id}/refund/approve")
    public ApiResponse<?> approveRefund(@PathVariable Long id) {
        jdbc.update("update orders set refund_status='APPROVED' where id=?", id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/orders/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        jdbc.update("update orders set status=? where id=?", status, id);
        jdbc.update("insert into order_logistics(order_id,content,created_at) values(?,?,?)", id, "管理员将订单状态更新为 " + status, LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @GetMapping("/orders/export")
    public void exportOrders(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=orders.csv");
        response.getWriter().println("orderNo,userId,totalAmount,status,paymentStatus,createdAt");
        for (Map<String, Object> row : jdbc.queryForList(orderSelect() + " order by created_at desc,id desc")) {
            response.getWriter().printf("%s,%s,%s,%s,%s,%s%n", row.get("orderNo"), row.get("userId"), row.get("totalAmount"), row.get("status"), row.get("paymentStatus"), row.get("createdAt"));
        }
    }

    @GetMapping("/dashboard/export")
    public void exportDashboard(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=dashboard.csv");
        response.getWriter().println("metric,value");
        response.getWriter().println("userCount," + value("select count(*) from user"));
        response.getWriter().println("orderCount," + value("select count(*) from orders"));
    }

    private Integer value(String sql) {
        Integer value = jdbc.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private String orderSelect() {
        return """
                select id,order_no orderNo,user_id userId,address_id addressId,total_amount totalAmount,status,
                       payment_status paymentStatus,logistics_status logisticsStatus,refund_status refundStatus,
                       discount_amount discountAmount,payment_method paymentMethod,created_at createdAt
                from orders
                """;
    }
}
