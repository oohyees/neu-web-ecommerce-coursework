package com.example.ecommerce.controller;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.CurrentSession;
import com.example.ecommerce.common.SessionToken;
import com.example.ecommerce.model.SessionInfo;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
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
        if (user == null) return ApiResponse.fail("账号或密码错误");
        String token = sessionService.create(user.getId(), "USER");
        // Bind Shiro Subject so @RequiresRoles works
        try { SecurityUtils.getSubject().login(new SessionToken(new SessionInfo(user.getId(), "USER"))); }
        catch (Exception ignored) {}
        return ApiResponse.ok(Map.of("userId", user.getId(), "nickname", user.getNickname(), "token", token));
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
        User user=new User(); user.setUsername(body.get("username")); user.setPassword(body.get("password")); user.setNickname(body.get("nickname")); user.setEmail(body.get("email")); user.setPhone(body.get("phone")); user.setAvatarUrl("/catalog/avatar-default.svg");
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
    public ApiResponse<?> profile(HttpServletRequest request) {
        return ApiResponse.ok(authService.getUser(CurrentSession.userId(request)));
    }

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@RequestBody User user, HttpServletRequest request) {
        user.setId(CurrentSession.userId(request));
        authService.updateProfile(user);
        return ApiResponse.ok(authService.getUser(user.getId()));
    }

    @PutMapping("/password")
    public ApiResponse<?> changePassword(@RequestBody Map<String,String> body, HttpServletRequest request){
        int updated=authService.updatePasswordById(CurrentSession.userId(request),body.get("oldPassword"),body.get("newPassword"));
        return updated==0?ApiResponse.fail("原密码错误"):ApiResponse.ok(null);
    }

    @PostMapping("/admin/login")
    public ApiResponse<?> adminLogin(@RequestBody Map<String, String> body) {
        Map<String,Object> admin = authService.loginAdmin(body.get("username"), body.get("password"));
        if (admin == null) return ApiResponse.fail("管理员账号或密码错误");
        String role = String.valueOf(admin.get("role"));
        Long id = Long.valueOf(String.valueOf(admin.get("id")));
        String token = sessionService.create(id, role);
        // Bind Shiro Subject so @RequiresRoles works
        try { SecurityUtils.getSubject().login(new SessionToken(new SessionInfo(id, role))); }
        catch (Exception ignored) {}
        return ApiResponse.ok(Map.of("adminId", id, "role", role, "token", token));
    }

    @PutMapping("/admin/password")
    public ApiResponse<?> changeAdminPassword(@RequestBody Map<String,String> body, HttpServletRequest request){
        int updated=authService.updateAdminPassword(CurrentSession.adminId(request),body.get("oldPassword"),body.get("newPassword"));
        return updated==0?ApiResponse.fail("原密码错误"):ApiResponse.ok(null);
    }
    @GetMapping("/admin/profile")
    public ApiResponse<?> adminProfile(HttpServletRequest request){return ApiResponse.ok(authService.getAdminProfile(CurrentSession.adminId(request)));}
    @PutMapping("/admin/profile")
    public ApiResponse<?> updateAdminProfile(@RequestBody Map<String,String> body, HttpServletRequest request){
        long adminId = CurrentSession.adminId(request);
        authService.updateAdminProfile(adminId,body.get("nickname"),body.get("email"),body.get("phone"));
        return ApiResponse.ok(authService.getAdminProfile(adminId));
    }

    @GetMapping("/admin/users")
    @RequiresPermissions("user:manage")
    public ApiResponse<?> users(@RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        int offset = Math.max(0, (page - 1)) * size;
        return ApiResponse.ok(Map.of(
                "items", authService.findUsers(keyword, offset, size),
                "total", authService.countUsers(keyword)
        ));
    }

    @GetMapping("/admin/users/{id}")
    public ApiResponse<?> userDetail(@PathVariable Long id) {
        User user = authService.getUser(id);
        return user == null ? ApiResponse.fail("用户不存在") : ApiResponse.ok(user);
    }

    @PutMapping("/admin/users/{id}/enabled")
    public ApiResponse<?> updateEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        authService.updateEnabled(id, enabled);
        return ApiResponse.ok(null);
    }

    @PostMapping("/admin/users")
    public ApiResponse<?> createUser(@RequestBody Map<String, String> body) {
        if (body.get("username") == null || body.get("password") == null) return ApiResponse.fail("用户名和密码必填");
        User user = new User();
        user.setUsername(body.get("username"));
        user.setPassword(body.get("password"));
        user.setNickname(body.getOrDefault("nickname", ""));
        user.setEmail(body.getOrDefault("email", ""));
        user.setPhone(body.getOrDefault("phone", ""));
        authService.register(user);
        return ApiResponse.ok(Map.of("id", user.getId()));
    }

    @PutMapping("/admin/users/{id}")
    public ApiResponse<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        authService.getAuthMapper().updateUser(id,
                body.getOrDefault("nickname", ""),
                body.getOrDefault("email", ""),
                body.getOrDefault("phone", ""));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/admin/users/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        authService.getAuthMapper().deleteUser(id);
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
    @RequiresPermissions("admin:manage")
    public ApiResponse<?> listAdmins(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(authService.getAuthMapper().findAdmins(keyword));
    }

    @PostMapping("/admin/admins")
    public ApiResponse<?> createAdmin(@RequestBody Map<String, String> body) {
        if (body.get("username") == null || body.get("password") == null) return ApiResponse.fail("用户名和密码必填");
        String role = body.getOrDefault("role", "ADMIN");
        if (!java.util.Set.of("ADMIN", "SUPER_ADMIN").contains(role)) return ApiResponse.fail("无效的角色");
        authService.getAuthMapper().insertAdmin(body.get("username"), authService.encodePassword(body.get("password")), body.getOrDefault("nickname", ""), body.getOrDefault("email", ""), body.getOrDefault("phone", ""), role);
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
