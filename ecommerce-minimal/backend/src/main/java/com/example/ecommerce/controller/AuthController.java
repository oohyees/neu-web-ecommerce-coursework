package com.example.ecommerce.controller;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.service.SessionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private final AuthService authService;
    private final SessionService sessionService;
    private final JdbcTemplate jdbc;
    private final RedisTemplate<String,Object> redisTemplate;
    private final com.example.ecommerce.service.MailService mailService;
    public AuthController(AuthService authService, SessionService sessionService, JdbcTemplate jdbc, RedisTemplate<String,Object> redisTemplate, com.example.ecommerce.service.MailService mailService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.jdbc = jdbc;
        this.redisTemplate = redisTemplate;
        this.mailService = mailService;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> body) {
        User user = authService.loginUser(body.get("username"), body.get("password"));
        return user == null ? ApiResponse.fail("账号或密码错误") : ApiResponse.ok(Map.of(
                "userId", user.getId(),
                "nickname", user.getNickname(),
                "token", sessionService.create(user.getId(), "USER")
        ));
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody User user) {
        User saved = authService.register(user);
        return saved == null ? ApiResponse.fail("用户名已存在") : ApiResponse.ok(Map.of("userId", saved.getId()));
    }

    @PostMapping("/code")
    public ApiResponse<?> sendCode(@RequestBody Map<String,String> body){
        String email=body.get("email"), purpose=body.getOrDefault("purpose","REGISTER");
        String throttleKey="verify:throttle:"+purpose+":"+email;
        if(Boolean.TRUE.equals(redisTemplate.hasKey(throttleKey))) return ApiResponse.fail("发送过于频繁，请稍后再试");
        String code=String.valueOf((int)(Math.random()*900000)+100000);
        try { mailService.sendVerificationCode(email,code,purpose); }
        catch (Exception ex) { return ApiResponse.fail("验证码发送失败："+ex.getMessage()); }
        redisTemplate.opsForValue().set("verify:"+purpose+":"+email,code,java.time.Duration.ofMinutes(10));
        redisTemplate.opsForValue().set(throttleKey,"1",java.time.Duration.ofMinutes(1));
        return ApiResponse.ok(null);
    }

    @PostMapping("/register/email")
    public ApiResponse<?> registerByEmail(@RequestBody Map<String,String> body){
        Object valid=redisTemplate.opsForValue().get("verify:REGISTER:"+body.get("email"));
        if(valid==null || !body.get("code").equals(String.valueOf(valid))) return ApiResponse.fail("验证码无效");
        User user=new User(); user.setUsername(body.get("username")); user.setPassword(body.get("password")); user.setNickname(body.get("nickname")); user.setEmail(body.get("email")); user.setPhone(body.get("phone")); user.setAvatarUrl("https://dummyimage.com/120x120/dbeafe/1e3a8a&text=U");
        return register(user);
    }

    @PostMapping("/password/reset")
    public ApiResponse<?> resetPassword(@RequestBody Map<String,String> body){
        Object valid=redisTemplate.opsForValue().get("verify:RESET:"+body.get("email"));
        if(valid==null || !body.get("code").equals(String.valueOf(valid))) return ApiResponse.fail("验证码无效");
        if(authService.getUserByEmail(body.get("email"))==null) return ApiResponse.fail("邮箱未注册");
        authService.updatePassword(body.get("email"),body.get("password"));
        return ApiResponse.ok(null);
    }

    @GetMapping("/profile")
    public ApiResponse<?> profile(@RequestParam Long userId) {
        return ApiResponse.ok(authService.getUser(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@RequestBody User user) {
        authService.updateProfile(user);
        return ApiResponse.ok(authService.getUser(user.getId()));
    }

    @PutMapping("/password")
    public ApiResponse<?> changePassword(@RequestBody Map<String,String> body){
        int updated=authService.updatePasswordById(Long.valueOf(body.get("userId")),body.get("oldPassword"),body.get("newPassword"));
        return updated==0?ApiResponse.fail("原密码错误"):ApiResponse.ok(null);
    }

    @PostMapping("/admin/login")
    public ApiResponse<?> adminLogin(@RequestBody Map<String, String> body) {
        Map<String,Object> admin = authService.loginAdmin(body.get("username"), body.get("password"));
        return admin == null ? ApiResponse.fail("管理员账号或密码错误") : ApiResponse.ok(Map.of(
                "adminId", admin.get("id"),
                "role", admin.get("role"),
                "token", sessionService.create(Long.valueOf(String.valueOf(admin.get("id"))), String.valueOf(admin.get("role")))
        ));
    }

    @PutMapping("/admin/password")
    public ApiResponse<?> changeAdminPassword(@RequestBody Map<String,String> body){
        int updated=authService.updateAdminPassword(Long.valueOf(body.get("adminId")),body.get("oldPassword"),body.get("newPassword"));
        return updated==0?ApiResponse.fail("原密码错误"):ApiResponse.ok(null);
    }
    @GetMapping("/admin/profile")
    public ApiResponse<?> adminProfile(@RequestParam Long adminId){return ApiResponse.ok(authService.getAdminProfile(adminId));}
    @PutMapping("/admin/profile")
    public ApiResponse<?> updateAdminProfile(@RequestBody Map<String,String> body){
        authService.updateAdminProfile(Long.valueOf(body.get("adminId")),body.get("nickname"),body.get("email"),body.get("phone"));
        return ApiResponse.ok(authService.getAdminProfile(Long.valueOf(body.get("adminId"))));
    }

    @GetMapping("/admin/users")
    public ApiResponse<?> users(@RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        int offset = (page - 1) * size;
        return ApiResponse.ok(Map.of(
                "items", authService.findUsers(keyword, offset, size),
                "total", authService.countUsers(keyword)
        ));
    }

    @PutMapping("/admin/users/{id}/enabled")
    public ApiResponse<?> updateEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        authService.updateEnabled(id, enabled);
        return ApiResponse.ok(null);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token != null && !token.isEmpty()) sessionService.delete(token);
        return ApiResponse.ok(null);
    }

    // 管理员账号管理（仅 SUPER_ADMIN）
    @GetMapping("/admin/admins")
    public ApiResponse<?> listAdmins(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(authService.getAuthMapper().findAdmins(keyword));
    }

    @PostMapping("/admin/admins")
    public ApiResponse<?> createAdmin(@RequestBody Map<String, String> body) {
        if (body.get("username") == null || body.get("password") == null) return ApiResponse.fail("用户名和密码必填");
        String role = body.getOrDefault("role", "ADMIN");
        if (!java.util.Set.of("ADMIN", "SUPER_ADMIN").contains(role)) return ApiResponse.fail("无效的角色");
        authService.getAuthMapper().insertAdmin(body.get("username"), body.get("password"), body.getOrDefault("nickname", ""), body.getOrDefault("email", ""), body.getOrDefault("phone", ""), role);
        return ApiResponse.ok(null);
    }

    @PutMapping("/admin/admins")
    public ApiResponse<?> updateAdmin(@RequestBody Map<String, String> body) {
        authService.getAuthMapper().updateAdmin(Long.valueOf(body.get("id")), body.getOrDefault("nickname", ""), body.getOrDefault("email", ""), body.getOrDefault("phone", ""), body.getOrDefault("role", "ADMIN"));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/admin/admins/{id}")
    public ApiResponse<?> deleteAdmin(@PathVariable Long id) {
        authService.getAuthMapper().deleteAdmin(id);
        return ApiResponse.ok(null);
    }
}
