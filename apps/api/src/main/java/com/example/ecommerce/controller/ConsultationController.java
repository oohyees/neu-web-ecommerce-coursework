package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.CurrentSession;
import com.example.ecommerce.mapper.ConsultationMapper;
import com.example.ecommerce.model.CustomerConsultation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
@RestController @CrossOrigin
public class ConsultationController {
  private final ConsultationMapper mapper;
  public ConsultationController(ConsultationMapper mapper){this.mapper=mapper;}
  @GetMapping("/api/consultations") public ApiResponse<?> mine(@RequestParam(required=false) Long userId, HttpServletRequest request){return ApiResponse.ok(mapper.findByUserId(CurrentSession.userId(request)));}
  @PostMapping("/api/consultations") public ApiResponse<?> create(@RequestBody CustomerConsultation item, HttpServletRequest request){item.setUserId(CurrentSession.userId(request)); item.setStatus("PENDING"); item.setCreatedAt(LocalDateTime.now()); mapper.insert(item); return ApiResponse.ok(item);}
  @GetMapping("/api/admin/consultations") public ApiResponse<?> all(@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer size){var all=mapper.findAll(); int from=Math.min((page-1)*size,all.size()),to=Math.min(from+size,all.size()); return ApiResponse.ok(Map.of("items",all.subList(from,to),"total",all.size()));}
  @PutMapping("/api/admin/consultations") public ApiResponse<?> reply(@RequestBody CustomerConsultation item){mapper.reply(item); return ApiResponse.ok(item);}
  @PutMapping("/api/admin/consultations/{id}/processed") public ApiResponse<?> processed(@PathVariable Long id){mapper.markProcessed(id); return ApiResponse.ok(null);}
}
