package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.ActivityNoticeMapper;
import com.example.ecommerce.model.ActivityNotice;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
@RestController @CrossOrigin
public class ActivityNoticeController {
  private final ActivityNoticeMapper mapper;
  public ActivityNoticeController(ActivityNoticeMapper mapper){this.mapper=mapper;}
  @GetMapping("/api/activity-notices") public ApiResponse<?> enabled(){return ApiResponse.ok(mapper.findEnabled());}
  @GetMapping("/api/admin/activity-notices") public ApiResponse<?> all(@RequestParam(required=false) String keyword){return ApiResponse.ok(mapper.findAll(keyword));}
  @PostMapping("/api/admin/activity-notices") public ApiResponse<?> create(@RequestBody ActivityNotice notice){notice.setCreatedAt(LocalDateTime.now()); if(notice.getEnabled()==null) notice.setEnabled(true); mapper.insert(notice); return ApiResponse.ok(notice);}
  @PutMapping("/api/admin/activity-notices") public ApiResponse<?> update(@RequestBody ActivityNotice notice){mapper.update(notice); return ApiResponse.ok(notice);}
  @DeleteMapping("/api/admin/activity-notices/{id}") public ApiResponse<?> delete(@PathVariable Long id){mapper.delete(id); return ApiResponse.ok(null);}
}
