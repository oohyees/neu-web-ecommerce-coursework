package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
@RestController @RequestMapping("/api/admin/dashboard") @CrossOrigin
public class DashboardController {
    private final JdbcTemplate jdbc;
    public DashboardController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping public ApiResponse<?> summary(){return ApiResponse.ok(Map.of(
      "userCount",jdbc.queryForObject("select count(*) from user",Long.class),
      "orderCount",jdbc.queryForObject("select count(*) from orders",Long.class),
      "salesAmount",jdbc.queryForObject("select coalesce(sum(total_amount),0) from orders where status <> 'CANCELLED'",BigDecimal.class),
      "productCount",jdbc.queryForObject("select count(*) from product",Long.class),
      "todayOrderCount",jdbc.queryForObject("select count(*) from orders where date(created_at)=curdate()",Long.class),
      "todaySalesAmount",jdbc.queryForObject("select coalesce(sum(total_amount),0) from orders where status <> 'CANCELLED' and date(created_at)=curdate()",BigDecimal.class),
      "salesTrend",jdbc.queryForList("select date(created_at) day, coalesce(sum(total_amount),0) amount from orders group by date(created_at) order by day desc limit 7"),
      "hotProducts",jdbc.queryForList("select name,sales from product order by sales desc,id desc limit 5"),
      "orderStatus",jdbc.queryForList("select status,count(*) value from orders group by status")
    ));}
    @GetMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response) throws Exception{
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      response.setHeader("Content-Disposition","attachment; filename=dashboard.xlsx");
      Workbook wb=new XSSFWorkbook(); Sheet s=wb.createSheet("dashboard");
      Object[][] rows={{"指标","数值"},{"用户数",jdbc.queryForObject("select count(*) from user",Long.class)},{"订单数",jdbc.queryForObject("select count(*) from orders",Long.class)},{"销售额",jdbc.queryForObject("select coalesce(sum(total_amount),0) from orders where status <> 'CANCELLED'",BigDecimal.class)},{"今日订单",jdbc.queryForObject("select count(*) from orders where date(created_at)=curdate()",Long.class)},{"今日销售额",jdbc.queryForObject("select coalesce(sum(total_amount),0) from orders where status <> 'CANCELLED' and date(created_at)=curdate()",BigDecimal.class)}};
      for(int i=0;i<rows.length;i++){Row r=s.createRow(i);r.createCell(0).setCellValue(String.valueOf(rows[i][0]));r.createCell(1).setCellValue(String.valueOf(rows[i][1]));}
      wb.write(response.getOutputStream());wb.close();
    }
}
