package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.AnnouncementMapper;
import com.example.ecommerce.model.Announcement;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
@RestController @CrossOrigin
public class AnnouncementController {
    private final AnnouncementMapper mapper;
    public AnnouncementController(AnnouncementMapper mapper){this.mapper=mapper;}
    @GetMapping("/api/announcements") public ApiResponse<?> list(){return ApiResponse.ok(mapper.findAll());}
    @PostMapping("/api/admin/announcements") public ApiResponse<?> create(@RequestBody Announcement a){a.setCreatedAt(LocalDateTime.now());mapper.insert(a);return ApiResponse.ok(a);}
    @PutMapping("/api/admin/announcements") public ApiResponse<?> update(@RequestBody Announcement a){mapper.update(a);return ApiResponse.ok(a);}
    @DeleteMapping("/api/admin/announcements/{id}") public ApiResponse<?> delete(@PathVariable Long id){mapper.delete(id);return ApiResponse.ok(null);}
}
