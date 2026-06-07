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
    private final MailService mailService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(JdbcTemplate jdbc, StringRedisTemplate redis, MailService mailService) {
        this.jdbc = jdbc;
        this.redis = redis;
        this.mailService = mailService;
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
                """, body.get("username"), encoder.encode(body.get("password")), body.getOrDefault("nickname", body.get("username")),
                body.getOrDefault("email", ""), body.getOrDefault("phone", ""), "/catalog/avatar-default.svg");
        Long id = jdbc.queryForObject("select id from user where username=?", Long.class, body.get("username"));
        return ApiResponse.ok(Map.of("userId", id));
    }

    @PostMapping("/register/email")
    public ApiResponse<?> registerEmail(@RequestBody Map<String, String> body) {
        if (!validCode(body.get("email"), body.get("code"), "REGISTER")) return ApiResponse.fail("验证码无效");
        return register(body);
    }

    @PostMapping("/code")
    public ApiResponse<?> code(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String purpose = body.getOrDefault("purpose", "REGISTER");
        if (email == null || email.isBlank()) return ApiResponse.fail("邮箱不能为空");
        String throttleKey = "verify:throttle:" + purpose + ":" + email;
        if (Boolean.TRUE.equals(redis.hasKey(throttleKey))) return ApiResponse.fail("发送过于频繁，请稍后再试");
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        try {
            mailService.sendVerificationCode(email, code, purpose);
        } catch (Exception ex) {
            return ApiResponse.fail("验证码发送失败：" + ex.getMessage());
        }
        redis.opsForValue().set("verify:" + purpose + ":" + email, code, Duration.ofMinutes(10));
        redis.opsForValue().set(throttleKey, "1", Duration.ofMinutes(1));
        return ApiResponse.ok(null);
    }

    @PostMapping("/password/reset")
    public ApiResponse<?> resetPassword(@RequestBody Map<String, String> body) {
        if (!validCode(body.get("email"), body.get("code"), "RESET")) return ApiResponse.fail("验证码无效");
        int updated = jdbc.update("update user set password=? where email=?", encoder.encode(body.get("password")), body.get("email"));
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

    @PutMapping("/password")
    public ApiResponse<?> changePassword(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, String> body) {
        var rows = jdbc.queryForList("select password from user where id=?", userId);
        if (rows.isEmpty() || !matches(String.valueOf(rows.get(0).get("password")), body.get("oldPassword"))) {
            return ApiResponse.fail("原密码错误");
        }
        jdbc.update("update user set password=? where id=?", encoder.encode(body.get("newPassword")), userId);
        return ApiResponse.ok(null);
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
        listArgs.add(Math.max(0, (page - 1)) * size);
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

    @GetMapping("/admin/profile")
    public ApiResponse<?> adminProfile(@RequestHeader("X-User-Id") Long adminId) {
        var admins = jdbc.queryForList("select id,username,nickname,email,phone,role from admin_user where id=?", adminId);
        return admins.isEmpty() ? ApiResponse.fail("管理员不存在") : ApiResponse.ok(admins.get(0));
    }

    @PutMapping("/admin/profile")
    public ApiResponse<?> updateAdminProfile(@RequestHeader("X-User-Id") Long adminId, @RequestBody Map<String, String> body) {
        jdbc.update("update admin_user set nickname=?,email=?,phone=? where id=?",
                body.getOrDefault("nickname", ""), body.getOrDefault("email", ""), body.getOrDefault("phone", ""), adminId);
        return adminProfile(adminId);
    }

    @PutMapping("/admin/password")
    public ApiResponse<?> changeAdminPassword(@RequestHeader("X-User-Id") Long adminId, @RequestBody Map<String, String> body) {
        var rows = jdbc.queryForList("select password from admin_user where id=?", adminId);
        if (rows.isEmpty() || !matches(String.valueOf(rows.get(0).get("password")), body.get("oldPassword"))) {
            return ApiResponse.fail("原密码错误");
        }
        jdbc.update("update admin_user set password=? where id=?", encoder.encode(body.get("newPassword")), adminId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/admin/admins")
    public ApiResponse<?> admins(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ApiResponse.ok(jdbc.queryForList("select id,username,nickname,email,phone,role from admin_user order by id"));
        }
        String like = "%" + keyword + "%";
        return ApiResponse.ok(jdbc.queryForList("""
                select id,username,nickname,email,phone,role from admin_user
                where username like ? or nickname like ? or email like ? or phone like ? order by id
                """, like, like, like, like));
    }

    @PostMapping("/admin/admins")
    public ApiResponse<?> createAdmin(@RequestBody Map<String, String> body) {
        String role = body.getOrDefault("role", "ADMIN");
        if (!java.util.Set.of("ADMIN", "SUPER_ADMIN").contains(role)) return ApiResponse.fail("无效的角色");
        jdbc.update("""
                insert into admin_user(username,password,nickname,email,phone,role)
                values(?,?,?,?,?,?)
                """, body.get("username"), encoder.encode(body.get("password")), body.getOrDefault("nickname", ""),
                body.getOrDefault("email", ""), body.getOrDefault("phone", ""), role);
        return ApiResponse.ok(null);
    }

    @PutMapping("/admin/admins")
    public ApiResponse<?> updateAdmin(@RequestBody Map<String, String> body) {
        jdbc.update("update admin_user set nickname=?,email=?,phone=?,role=? where id=?",
                body.getOrDefault("nickname", ""), body.getOrDefault("email", ""), body.getOrDefault("phone", ""),
                body.getOrDefault("role", "ADMIN"), Long.valueOf(body.get("id")));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/admin/admins/{id}")
    public ApiResponse<?> deleteAdmin(@PathVariable Long id) {
        jdbc.update("delete from admin_user where id=?", id);
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

    private boolean validCode(String email, String code, String purpose) {
        if (email == null || email.isBlank() || code == null || code.isBlank()) return false;
        String key = "verify:" + purpose + ":" + email;
        String stored = redis.opsForValue().get(key);
        if (!code.equals(stored)) return false;
        redis.delete(key);
        return true;
    }

    private boolean matches(String stored, String raw) {
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) return encoder.matches(raw, stored);
        return stored.equals(raw);
    }
}
