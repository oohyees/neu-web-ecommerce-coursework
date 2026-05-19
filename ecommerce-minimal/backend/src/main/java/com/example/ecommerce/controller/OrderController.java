package com.example.ecommerce.controller;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.service.OrderService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@CrossOrigin
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final JdbcTemplate jdbc;
    public OrderController(OrderService orderService, OrderMapper orderMapper, JdbcTemplate jdbc) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.jdbc = jdbc;
    }

    @PostMapping("/api/orders")
    public ApiResponse<?> create(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(String.valueOf(body.get("userId")));
            Long addressId = Long.valueOf(String.valueOf(body.get("addressId")));
            java.util.List<Long> productIds = body.get("productIds") instanceof java.util.List<?> raw
                    ? raw.stream().map(v -> Long.valueOf(String.valueOf(v))).toList()
                    : java.util.List.of();
            Long couponId = body.get("couponId")==null?null:Long.valueOf(String.valueOf(body.get("couponId")));
            String paymentMethod = body.get("paymentMethod")==null?"MOCK_PAY":String.valueOf(body.get("paymentMethod"));
            return ApiResponse.ok(Map.of("orderNo", orderService.createOrder(userId, addressId, productIds, couponId, paymentMethod)));
        } catch (IllegalStateException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    @GetMapping("/api/admin/orders")
    public ApiResponse<?> adminOrders(@RequestParam(required = false) String keyword,@RequestParam(required = false) String status,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer size) {
        var all=orderMapper.searchAdmin(keyword,status);
        int from=Math.min((page-1)*size,all.size()),to=Math.min(from+size,all.size());
        return ApiResponse.ok(Map.of("items",all.subList(from,to),"total",all.size()));
    }

    @GetMapping("/api/orders")
    public ApiResponse<?> myOrders(@RequestParam Long userId,@RequestParam(required = false) String status,@RequestParam(required = false) String paymentStatus) {
        return ApiResponse.ok(orderMapper.findByUserId(userId,status).stream().filter(o->paymentStatus==null||paymentStatus.isBlank()||paymentStatus.equals(o.getPaymentStatus())).toList());
    }

    @GetMapping("/api/orders/{id}")
    public ApiResponse<?> detail(@PathVariable Long id){
        var order=orderMapper.findById(id);
        var items=jdbc.queryForList("select * from order_item where order_id=? order by id",id);
        var address=jdbc.queryForMap("select * from user_address where id=?",order.getAddressId());
        var logistics=orderMapper.findLogistics(id);
        return ApiResponse.ok(Map.of("order",order,"items",items,"address",address,"logistics",logistics));
    }

    @PutMapping("/api/orders/{id}/cancel")
    public ApiResponse<?> cancel(@PathVariable Long id) {
        orderMapper.updateStatus(id, "CANCELLED");
        orderMapper.updateLogisticsStatus(id, "已取消");
        orderMapper.insertLogistics(id,"订单已取消",LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/orders/{id}/ship")
    public ApiResponse<?> ship(@PathVariable Long id) {
        orderMapper.updateStatus(id, "SHIPPED");
        orderMapper.updateLogisticsStatus(id, "已发货");
        orderMapper.insertLogistics(id,"商家已发货",LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/orders/{id}/confirm")
    public ApiResponse<?> confirm(@PathVariable Long id) {
        orderMapper.updateStatus(id, "COMPLETED");
        orderMapper.updateLogisticsStatus(id, "已签收");
        orderMapper.insertLogistics(id,"用户已确认收货",LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/orders/{id}/pay")
    public ApiResponse<?> pay(@PathVariable Long id) {
        orderMapper.updatePaymentStatus(id, "PAID");
        orderMapper.updateLogisticsStatus(id, "待发货");
        orderMapper.insertLogistics(id,"订单已支付",LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/orders/{id}/pay-gateway")
    public ApiResponse<?> payGateway(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String method = body.getOrDefault("method", "MOCK_PAY");
        orderMapper.insertLogistics(id, "通过" + method + "完成支付", LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/orders/{id}/refund")
    public ApiResponse<?> refund(@PathVariable Long id) {
        orderMapper.updateRefundStatus(id, "REQUESTED");
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/orders/{id}/refund/approve")
    public ApiResponse<?> approveRefund(@PathVariable Long id) {
        orderMapper.updateRefundStatus(id, "APPROVED");
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/orders/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id,@RequestParam String status){
        orderMapper.updateStatus(id,status);
        orderMapper.insertLogistics(id,"管理员将订单状态更新为 "+status,LocalDateTime.now());
        return ApiResponse.ok(null);
    }

    @GetMapping("/api/orders/{id}/logistics")
    public ApiResponse<?> logistics(@PathVariable Long id){return ApiResponse.ok(orderMapper.findLogistics(id));}

    @GetMapping("/api/admin/orders/export")
    public void export(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=orders.xlsx");
        Workbook wb=new XSSFWorkbook(); Sheet sheet=wb.createSheet("orders");
        Row head=sheet.createRow(0); String[] headers={"orderNo","userId","totalAmount","status","paymentStatus","logisticsStatus","refundStatus","createdAt"};
        for(int i=0;i<headers.length;i++) head.createCell(i).setCellValue(headers[i]);
        int rowNum=1;
        for(var order:orderMapper.findAll()){
            Row row=sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getOrderNo()); row.createCell(1).setCellValue(order.getUserId());
            row.createCell(2).setCellValue(order.getTotalAmount().doubleValue()); row.createCell(3).setCellValue(order.getStatus());
            row.createCell(4).setCellValue(order.getPaymentStatus()); row.createCell(5).setCellValue(order.getLogisticsStatus());
            row.createCell(6).setCellValue(order.getRefundStatus()); row.createCell(7).setCellValue(String.valueOf(order.getCreatedAt()));
        }
        wb.write(response.getOutputStream()); wb.close();
    }
}
