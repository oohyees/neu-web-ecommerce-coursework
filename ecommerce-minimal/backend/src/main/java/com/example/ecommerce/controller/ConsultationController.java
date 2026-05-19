package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.ConsultationMapper;
import com.example.ecommerce.model.CustomerConsultation;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
@RestController @CrossOrigin
public class ConsultationController {
  private final ConsultationMapper mapper;
  public ConsultationController(ConsultationMapper mapper){this.mapper=mapper;}
  @GetMapping("/api/consultations") public ApiResponse<?> mine(@RequestParam Long userId){return ApiResponse.ok(mapper.findByUserId(userId));}
  @PostMapping("/api/consultations") public ApiResponse<?> create(@RequestBody CustomerConsultation item){item.setStatus("PENDING"); item.setCreatedAt(LocalDateTime.now()); mapper.insert(item); return ApiResponse.ok(item);}
  @GetMapping("/api/admin/consultations") public ApiResponse<?> all(@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer size){var all=mapper.findAll(); int from=Math.min((page-1)*size,all.size()),to=Math.min(from+size,all.size()); return ApiResponse.ok(Map.of("items",all.subList(from,to),"total",all.size()));}
  @PutMapping("/api/admin/consultations") public ApiResponse<?> reply(@RequestBody CustomerConsultation item){mapper.reply(item); return ApiResponse.ok(item);}
  @PutMapping("/api/admin/consultations/{id}/processed") public ApiResponse<?> processed(@PathVariable Long id){mapper.markProcessed(id); return ApiResponse.ok(null);}
}
