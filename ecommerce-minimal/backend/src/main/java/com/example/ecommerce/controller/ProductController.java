package com.example.ecommerce.controller;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.ProductMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {
    private final ProductMapper productMapper;
    public ProductController(ProductMapper productMapper) { this.productMapper = productMapper; }
    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String searchMode,
                               @RequestParam(required = false) String sort,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "9") Integer size) {
        int offset=(page-1)*size;
        return ApiResponse.ok(Map.of("items",productMapper.findAll(categoryId, keyword, searchMode, sort, offset, size),"total",productMapper.countAll(categoryId, keyword, searchMode)));
    }

    @GetMapping("/{id}")
    @Cacheable(value = "productDetail", key = "#id")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.ok(productMapper.findById(id));
    }

    @GetMapping("/admin/all")
    public ApiResponse<?> adminList(@RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String searchMode,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        int offset=(page-1)*size;
        return ApiResponse.ok(Map.of("items",productMapper.findAllForAdmin(categoryId, keyword, searchMode, sort, offset, size),"total",productMapper.countAllForAdmin(categoryId, keyword, searchMode)));
    }

    @PostMapping("/admin")
    public ApiResponse<?> create(@RequestBody com.example.ecommerce.model.Product product) {
        productMapper.insert(product);
        return ApiResponse.ok(product);
    }

    @PutMapping("/admin")
    public ApiResponse<?> update(@RequestBody com.example.ecommerce.model.Product product) {
        productMapper.update(product);
        return ApiResponse.ok(product);
    }

    @DeleteMapping("/admin/{id}")
    public ApiResponse<?> delete(@PathVariable Long id){
        productMapper.delete(id);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/admin/{id}/force")
    public ApiResponse<?> forceDelete(@PathVariable Long id){
        productMapper.forceDelete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/admin/export")
    public void export(jakarta.servlet.http.HttpServletResponse response) throws Exception{
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition","attachment; filename=products.xlsx");
        Workbook wb=new XSSFWorkbook(); Sheet sheet=wb.createSheet("products"); String[] headers={"id","categoryId","name","price","stock","sales"};
        Row head=sheet.createRow(0); for(int i=0;i<headers.length;i++)head.createCell(i).setCellValue(headers[i]);
        int r=1; for(var p:productMapper.findAllForAdmin(null,null,null,null,0,1000)){Row row=sheet.createRow(r++);row.createCell(0).setCellValue(p.getId());row.createCell(1).setCellValue(p.getCategoryId());row.createCell(2).setCellValue(p.getName());row.createCell(3).setCellValue(p.getPrice().doubleValue());row.createCell(4).setCellValue(p.getStock());row.createCell(5).setCellValue(p.getSales());}
        wb.write(response.getOutputStream());wb.close();
    }

    @PostMapping("/admin/import")
    public ApiResponse<?> importCsv(@RequestParam MultipartFile file) throws Exception{
        try(var br=new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String line; boolean first=true; int count=0;
            while((line=br.readLine())!=null){ if(first){first=false;continue;} String[] a=line.split(","); if(a.length<4) continue; var p=new com.example.ecommerce.model.Product(); p.setCategoryId(Long.valueOf(a[0])); p.setName(a[1]); p.setPrice(new java.math.BigDecimal(a[2])); p.setStock(Integer.valueOf(a[3])); p.setSales(0); p.setIsOnSale(true); p.setImageUrl(""); p.setDetailHtml(""); p.setParamsText(""); productMapper.insert(p); count++; }
            return ApiResponse.ok(Map.of("count",count));
        }
    }
}
