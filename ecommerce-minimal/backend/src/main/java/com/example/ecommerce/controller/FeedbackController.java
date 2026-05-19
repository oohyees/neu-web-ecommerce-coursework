package com.example.ecommerce.controller;
import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.FeedbackMapper;
import com.example.ecommerce.model.Feedback;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
@RestController @CrossOrigin
public class FeedbackController {
    private final FeedbackMapper mapper;
    public FeedbackController(FeedbackMapper mapper){this.mapper=mapper;}
    @GetMapping("/api/feedback") public ApiResponse<?> mine(@RequestParam Long userId){return ApiResponse.ok(mapper.findByUserId(userId));}
    @PostMapping("/api/feedback") public ApiResponse<?> create(@RequestBody Feedback f){f.setStatus("PENDING");f.setCreatedAt(LocalDateTime.now());mapper.insert(f);return ApiResponse.ok(f);}
    @GetMapping("/api/admin/feedback") public ApiResponse<?> all(@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer size){var all=mapper.findAll();int from=Math.min((page-1)*size,all.size()),to=Math.min(from+size,all.size());return ApiResponse.ok(java.util.Map.of("items",all.subList(from,to),"total",all.size()));}
    @PutMapping("/api/admin/feedback") public ApiResponse<?> reply(@RequestBody Feedback f){f.setStatus("REPLIED");mapper.reply(f);return ApiResponse.ok(f);}
    @PutMapping("/api/admin/feedback/{id}/processed") public ApiResponse<?> processed(@PathVariable Long id){mapper.markProcessed(id);return ApiResponse.ok(null);}
}
