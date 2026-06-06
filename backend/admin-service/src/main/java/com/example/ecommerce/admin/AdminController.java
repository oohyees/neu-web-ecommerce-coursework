package com.example.ecommerce.admin;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
                "userCount", value("select count(*) from ecommerce_auth.user"),
                "orderCount", value("select count(*) from ecommerce_order.orders"),
                "salesAmount", jdbc.queryForObject("select coalesce(sum(total_amount),0) from ecommerce_order.orders where payment_status='PAID'", java.math.BigDecimal.class),
                "todaySalesAmount", jdbc.queryForObject("select coalesce(sum(total_amount),0) from ecommerce_order.orders where payment_status='PAID' and date(created_at)=curdate()", java.math.BigDecimal.class),
                "salesTrend", jdbc.queryForList("select date(created_at) day,coalesce(sum(total_amount),0) amount from ecommerce_order.orders where payment_status='PAID' group by date(created_at) order by day desc limit 7"),
                "orderStatus", jdbc.queryForList("select status,count(*) value from ecommerce_order.orders group by status"),
                "hotProducts", jdbc.queryForList("select name,sales from product order by sales desc,id desc limit 8"),
                "refundRequests", value("select count(*) from ecommerce_order.orders where refund_status='REQUESTED'"),
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
        var items = jdbc.queryForList("select id,username,nickname,email,phone,enabled from ecommerce_auth.user" + where + " order by id desc limit ? offset ?", listArgs.toArray());
        Integer total = jdbc.queryForObject("select count(*) from ecommerce_auth.user" + where, Integer.class, args);
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
        Integer total = jdbc.queryForObject("select count(*) from ecommerce_order.orders" + where, Integer.class, args.toArray());
        args.add(size);
        args.add((page - 1) * size);
        var items = jdbc.queryForList(orderSelect() + where + " order by created_at desc,id desc limit ? offset ?", args.toArray());
        return ApiResponse.ok(Map.of("items", items, "total", total == null ? 0 : total));
    }

    @PutMapping("/orders/{id}/ship")
    public ApiResponse<?> ship(@PathVariable Long id) {
        jdbc.update("update ecommerce_order.orders set status='SHIPPED',logistics_status='已发货' where id=?", id);
        jdbc.update("insert into ecommerce_order.order_logistics(order_id,content,created_at) values(?,?,?)", id, "商家已发货", LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @PutMapping("/orders/{id}/refund/approve")
    public ApiResponse<?> approveRefund(@PathVariable Long id) {
        jdbc.update("update ecommerce_order.orders set refund_status='APPROVED' where id=?", id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/orders/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        jdbc.update("update ecommerce_order.orders set status=? where id=?", status, id);
        jdbc.update("insert into ecommerce_order.order_logistics(order_id,content,created_at) values(?,?,?)", id, "管理员将订单状态更新为 " + status, LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @GetMapping("/orders/export")
    public void exportOrders(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=orders.xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("orders");
            Row head = sheet.createRow(0);
            String[] headers = {"orderNo", "userId", "totalAmount", "status", "paymentStatus", "createdAt"};
            for (int i = 0; i < headers.length; i++) head.createCell(i).setCellValue(headers[i]);
            int r = 1;
            for (Map<String, Object> item : jdbc.queryForList(orderSelect() + " order by created_at desc,id desc")) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(String.valueOf(item.get("orderNo")));
                row.createCell(1).setCellValue(String.valueOf(item.get("userId")));
                row.createCell(2).setCellValue(String.valueOf(item.get("totalAmount")));
                row.createCell(3).setCellValue(String.valueOf(item.get("status")));
                row.createCell(4).setCellValue(String.valueOf(item.get("paymentStatus")));
                row.createCell(5).setCellValue(String.valueOf(item.get("createdAt")));
            }
            wb.write(response.getOutputStream());
        }
    }

    @GetMapping("/dashboard/export")
    public void exportDashboard(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=dashboard.xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("dashboard");
            Row head = sheet.createRow(0);
            head.createCell(0).setCellValue("metric");
            head.createCell(1).setCellValue("value");
            Row userRow = sheet.createRow(1);
            userRow.createCell(0).setCellValue("userCount");
            userRow.createCell(1).setCellValue(value("select count(*) from ecommerce_auth.user"));
            Row orderRow = sheet.createRow(2);
            orderRow.createCell(0).setCellValue("orderCount");
            orderRow.createCell(1).setCellValue(value("select count(*) from ecommerce_order.orders"));
            wb.write(response.getOutputStream());
        }
    }

    @PostMapping("/categories")
    public ApiResponse<?> createCategory(@RequestBody Map<String, Object> body) {
        jdbc.update("insert into product_category(parent_id,name,sort_order) values(?,?,?)",
                longValue(body.get("parentId")), stringValue(body.get("name")), intValue(body.get("sortOrder"), 0));
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("select id,parent_id parentId,name,sort_order sortOrder from product_category where id=?", id));
    }

    @PutMapping("/categories")
    public ApiResponse<?> updateCategory(@RequestBody Map<String, Object> body) {
        jdbc.update("update product_category set parent_id=?,name=?,sort_order=? where id=?",
                longValue(body.get("parentId")), stringValue(body.get("name")), intValue(body.get("sortOrder"), 0), longValue(body.get("id")));
        return ApiResponse.ok(body);
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<?> deleteCategory(@PathVariable Long id) {
        jdbc.update("delete from product_category where id=?", id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/announcements")
    public ApiResponse<?> createAnnouncement(@RequestBody Map<String, Object> body) {
        jdbc.update("insert into announcement(title,content,created_at) values(?,?,?)",
                stringValue(body.get("title")), stringValue(body.get("content")), LocalDateTime.now());
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("select id,title,content,created_at createdAt from announcement where id=?", id));
    }

    @PutMapping("/announcements")
    public ApiResponse<?> updateAnnouncement(@RequestBody Map<String, Object> body) {
        jdbc.update("update announcement set title=?,content=? where id=?",
                stringValue(body.get("title")), stringValue(body.get("content")), longValue(body.get("id")));
        return ApiResponse.ok(body);
    }

    @DeleteMapping("/announcements/{id}")
    public ApiResponse<?> deleteAnnouncement(@PathVariable Long id) {
        jdbc.update("delete from announcement where id=?", id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/activity-notices")
    public ApiResponse<?> activityNotices(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ApiResponse.ok(jdbc.queryForList("select id,title,content,enabled,created_at createdAt from activity_notice order by id desc"));
        }
        return ApiResponse.ok(jdbc.queryForList("""
                select id,title,content,enabled,created_at createdAt from activity_notice
                where title like ? or content like ? order by id desc
                """, "%" + keyword + "%", "%" + keyword + "%"));
    }

    @PostMapping("/activity-notices")
    public ApiResponse<?> createActivityNotice(@RequestBody Map<String, Object> body) {
        jdbc.update("insert into activity_notice(title,content,enabled,created_at) values(?,?,?,?)",
                stringValue(body.get("title")), stringValue(body.get("content")), boolValue(body.get("enabled")) ? 1 : 0, LocalDateTime.now());
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("select id,title,content,enabled,created_at createdAt from activity_notice where id=?", id));
    }

    @PutMapping("/activity-notices")
    public ApiResponse<?> updateActivityNotice(@RequestBody Map<String, Object> body) {
        jdbc.update("update activity_notice set title=?,content=?,enabled=? where id=?",
                stringValue(body.get("title")), stringValue(body.get("content")), boolValue(body.get("enabled")) ? 1 : 0, longValue(body.get("id")));
        return ApiResponse.ok(body);
    }

    @DeleteMapping("/activity-notices/{id}")
    public ApiResponse<?> deleteActivityNotice(@PathVariable Long id) {
        jdbc.update("delete from activity_notice where id=?", id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/feedback")
    public ApiResponse<?> feedback(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Integer total = value("select count(*) from feedback");
        var items = jdbc.queryForList("""
                select id,user_id userId,type,content,contact,reply,status,created_at createdAt
                from feedback order by id desc limit ? offset ?
                """, size, (page - 1) * size);
        return ApiResponse.ok(Map.of("items", items, "total", total));
    }

    @PutMapping("/feedback")
    public ApiResponse<?> replyFeedback(@RequestBody Map<String, Object> body) {
        jdbc.update("update feedback set reply=?,status='REPLIED' where id=?", stringValue(body.get("reply")), longValue(body.get("id")));
        return ApiResponse.ok(body);
    }

    @PutMapping("/feedback/{id}/processed")
    public ApiResponse<?> processFeedback(@PathVariable Long id) {
        jdbc.update("update feedback set status='PROCESSED' where id=?", id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/consultations")
    public ApiResponse<?> consultations(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Integer total = value("select count(*) from customer_consultation");
        var items = jdbc.queryForList("""
                select id,user_id userId,subject,content,reply,status,created_at createdAt
                from customer_consultation order by id desc limit ? offset ?
                """, size, (page - 1) * size);
        return ApiResponse.ok(Map.of("items", items, "total", total));
    }

    @PutMapping("/consultations")
    public ApiResponse<?> replyConsultation(@RequestBody Map<String, Object> body) {
        jdbc.update("update customer_consultation set reply=?,status='REPLIED' where id=?", stringValue(body.get("reply")), longValue(body.get("id")));
        return ApiResponse.ok(body);
    }

    @PutMapping("/consultations/{id}/processed")
    public ApiResponse<?> processConsultation(@PathVariable Long id) {
        jdbc.update("update customer_consultation set status='PROCESSED' where id=?", id);
        return ApiResponse.ok(null);
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
                from ecommerce_order.orders
                """;
    }

    private Long longValue(Object value) {
        return value == null || String.valueOf(value).isBlank() ? null : Long.valueOf(String.valueOf(value));
    }

    private int intValue(Object value, int fallback) {
        return value == null ? fallback : Integer.parseInt(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean boolValue(Object value) {
        if (value == null) return true;
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number number) return number.intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
