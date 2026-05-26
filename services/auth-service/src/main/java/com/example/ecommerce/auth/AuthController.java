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

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody Map<String, String> body) {
        Integer exists = jdbc.queryForObject("select count(*) from user where username=?", Integer.class, body.get("username"));
        if (exists != null && exists > 0) return ApiResponse.fail("用户名已存在");
        jdbc.update("""
                insert into user(username,password,nickname,email,phone,avatar_url,enabled)
                values(?,?,?,?,?,?,1)
                """, body.get("username"), body.get("password"), body.getOrDefault("nickname", body.get("username")),
                body.getOrDefault("email", ""), body.getOrDefault("phone", ""), "https://dummyimage.com/120x120/dbeafe/1e3a8a&text=U");
        Long id = jdbc.queryForObject("select id from user where username=?", Long.class, body.get("username"));
        return ApiResponse.ok(Map.of("userId", id));
    }

    @PostMapping("/register/email")
    public ApiResponse<?> registerEmail(@RequestBody Map<String, String> body) {
        return register(body);
    }

    @PostMapping("/code")
    public ApiResponse<?> code() {
        return ApiResponse.ok(Map.of("demoMode", true, "demoCode", "123456"));
    }

    @PostMapping("/password/reset")
    public ApiResponse<?> resetPassword(@RequestBody Map<String, String> body) {
        int updated = jdbc.update("update user set password=? where email=?", body.get("password"), body.get("email"));
        return updated == 0 ? ApiResponse.fail("邮箱未注册") : ApiResponse.ok(null);
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

    @GetMapping("/profile")
    public ApiResponse<?> profile(@RequestHeader("X-User-Id") Long userId) {
        var users = jdbc.queryForList("select id,username,nickname,email,phone,avatar_url avatarUrl,enabled from user where id=?", userId);
        return users.isEmpty() ? ApiResponse.fail("用户不存在") : ApiResponse.ok(users.get(0));
    }

    @GetMapping("/admin/users")
    public ApiResponse<?> users(@RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        String where = keyword == null || keyword.isBlank() ? "" : " where username like ? or nickname like ? or email like ? or phone like ?";
        Object[] args = keyword == null || keyword.isBlank() ? new Object[]{} :
                new Object[]{"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"};
        var listArgs = new java.util.ArrayList<>();
        java.util.Collections.addAll(listArgs, args);
        listArgs.add(size);
        listArgs.add((page - 1) * size);
        var items = jdbc.queryForList("select id,username,nickname,email,phone,enabled from user" + where + " order by id desc limit ? offset ?", listArgs.toArray());
        Integer total = jdbc.queryForObject("select count(*) from user" + where, Integer.class, args);
        return ApiResponse.ok(Map.of("items", items, "total", total == null ? 0 : total));
    }

    @PutMapping("/admin/users/{id}/enabled")
    public ApiResponse<?> enableUser(@PathVariable Long id, @RequestParam Boolean enabled) {
        jdbc.update("update user set enabled=? where id=?", enabled, id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/admin/users")
    public ApiResponse<?> createUser(@RequestBody Map<String, String> body) {
        return register(body);
    }

    @PutMapping("/admin/users/{id}")
    public ApiResponse<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        jdbc.update("update user set nickname=?,email=?,phone=? where id=?",
                body.getOrDefault("nickname", ""), body.getOrDefault("email", ""), body.getOrDefault("phone", ""), id);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/admin/users/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        jdbc.update("delete from user where id=?", id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, String> body) {
        jdbc.update("update user set nickname=?,email=?,phone=?,avatar_url=? where id=?",
                body.getOrDefault("nickname", ""), body.getOrDefault("email", ""),
                body.getOrDefault("phone", ""), body.getOrDefault("avatarUrl", ""), userId);
        return profile(userId);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token != null && !token.isBlank()) redis.delete("session:" + token);
        return ApiResponse.ok(null);
    }

    private boolean matches(String stored, String raw) {
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) return encoder.matches(raw, stored);
        return stored.equals(raw);
    }
}
