package com.example.ecommerce.admin;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class SupportController {
    private final JdbcTemplate jdbc;

    public SupportController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/feedback")
    public ApiResponse<?> myFeedback(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(jdbc.queryForList("""
                select id,user_id userId,type,content,contact,reply,status,created_at createdAt
                from feedback where user_id=? order by id desc
                """, userId));
    }

    @PostMapping("/feedback")
    public ApiResponse<?> createFeedback(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        jdbc.update("""
                insert into feedback(user_id,type,content,contact,status,created_at)
                values(?,?,?,?,?,?)
                """, userId, stringValue(body.get("type")), stringValue(body.get("content")),
                stringValue(body.get("contact")), "PENDING", LocalDateTime.now());
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("""
                select id,user_id userId,type,content,contact,reply,status,created_at createdAt
                from feedback where id=?
                """, id));
    }

    @GetMapping("/consultations")
    public ApiResponse<?> myConsultations(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(jdbc.queryForList("""
                select id,user_id userId,subject,content,reply,status,created_at createdAt
                from customer_consultation where user_id=? order by id desc
                """, userId));
    }

    @PostMapping("/consultations")
    public ApiResponse<?> createConsultation(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        jdbc.update("""
                insert into customer_consultation(user_id,subject,content,status,created_at)
                values(?,?,?,?,?)
                """, userId, stringValue(body.get("subject")), stringValue(body.get("content")), "PENDING", LocalDateTime.now());
        Long id = jdbc.queryForObject("select last_insert_id()", Long.class);
        return ApiResponse.ok(jdbc.queryForMap("""
                select id,user_id userId,subject,content,reply,status,created_at createdAt
                from customer_consultation where id=?
                """, id));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
