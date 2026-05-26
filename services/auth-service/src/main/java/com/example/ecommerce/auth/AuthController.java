package com.example.ecommerce.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JdbcTemplate jdbc;
    private final StringRedisTemplate redis;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(JdbcTemplate jdbc, StringRedisTemplate redis) {
        this.jdbc = jdbc;
        this.redis = redis;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> body) {
        var users = jdbc.queryForList("select id,password,nickname from user where username=? and enabled=1", body.get("username"));
        if (users.isEmpty() || !matches(String.valueOf(users.get(0).get("password")), body.get("password"))) return ApiResponse.fail("账号或密码错误");
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set("session:" + token, users.get(0).get("id") + ":USER", Duration.ofHours(12));
        return ApiResponse.ok(Map.of("userId", users.get(0).get("id"), "nickname", users.get(0).get("nickname"), "token", token));
    }

    @PostMapping("/admin/login")
    public ApiResponse<?> adminLogin(@RequestBody Map<String, String> body) {
        var admins = jdbc.queryForList("select id,password,role from admin_user where username=?", body.get("username"));
        if (admins.isEmpty() || !matches(String.valueOf(admins.get(0).get("password")), body.get("password"))) return ApiResponse.fail("管理员账号或密码错误");
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set("session:" + token, admins.get(0).get("id") + ":" + admins.get(0).get("role"), Duration.ofHours(12));
        return ApiResponse.ok(Map.of("adminId", admins.get(0).get("id"), "role", admins.get(0).get("role"), "token", token));
    }

    @GetMapping("/session")
    public ApiResponse<?> session(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = authorization == null ? "" : authorization.replace("Bearer ", "");
        String value = redis.opsForValue().get("session:" + token);
        if (value == null) return ApiResponse.fail("未登录");
        String[] parts = value.split(":");
        return ApiResponse.ok(Map.of("id", parts[0], "role", parts.length > 1 ? parts[1] : "USER"));
    }

    private boolean matches(String stored, String raw) {
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) return encoder.matches(raw, stored);
        return stored.equals(raw);
    }
}
