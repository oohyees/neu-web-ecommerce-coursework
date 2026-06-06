package com.example.ecommerce.service;

import com.example.ecommerce.mapper.AuthMapper;
import com.example.ecommerce.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public AuthService(AuthMapper authMapper) { this.authMapper = authMapper; }
    public User loginUser(String username, String password) {
        var credentials = authMapper.findUserCredentials(username);
        if (!passwordMatches(credentials, password)) return null;
        Long id = Long.valueOf(String.valueOf(credentials.get("id")));
        migratePlainPasswordIfNeeded(credentials, password, id, false);
        return authMapper.findUserById(id);
    }
    public User getUser(Long id) { return sanitize(authMapper.findUserById(id)); }
    public User getUserByEmail(String email){return authMapper.findUserByEmail(email);}
    public User register(User user) {
        if (authMapper.findUserByUsername(user.getUsername()) != null) return null;
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        authMapper.insertUser(user);
        return user;
    }
    public int updateProfile(User user) { return authMapper.updateProfile(user); }
    public int updatePassword(String email,String password){return authMapper.updatePassword(email,passwordEncoder.encode(password));}
    public int updatePasswordById(Long id,String oldPassword,String newPassword){
        User user = authMapper.findUserById(id);
        if (user == null || !passwordValueMatches(user.getPassword(), oldPassword)) return 0;
        return authMapper.updateUserPasswordHash(id, passwordEncoder.encode(newPassword));
    }
    public java.util.List<User> findUsers(String keyword, Integer offset, Integer size) { return authMapper.findUsers(keyword, offset, size).stream().map(this::sanitize).toList(); }
    public int countUsers(String keyword) { return authMapper.countUsers(keyword); }
    public int updateEnabled(Long id, Boolean enabled) { return authMapper.updateEnabled(id, enabled); }
    public java.util.Map<String,Object> loginAdmin(String username, String password) {
        var credentials = authMapper.findAdminCredentials(username);
        if (!passwordMatches(credentials, password)) return null;
        Long id = Long.valueOf(String.valueOf(credentials.get("id")));
        migratePlainPasswordIfNeeded(credentials, password, id, true);
        return authMapper.findAdminProfile(id);
    }
    public int updateAdminPassword(Long id,String oldPassword,String newPassword){
        var profile = authMapper.findAdminProfile(id);
        if (profile == null) return 0;
        var credentials = authMapper.findAdminCredentials(String.valueOf(profile.get("username")));
        if (!passwordMatches(credentials, oldPassword)) return 0;
        return authMapper.updateAdminPasswordHash(id, passwordEncoder.encode(newPassword));
    }
    public java.util.Map<String,Object> getAdminProfile(Long id){return authMapper.findAdminProfile(id);}
    public int updateAdminProfile(Long id,String nickname,String email,String phone){return authMapper.updateAdminProfile(id,nickname,email,phone);}
    public AuthMapper getAuthMapper(){return authMapper;}
    public String encodePassword(String rawPassword) { return passwordEncoder.encode(rawPassword); }

    private User sanitize(User user) {
        if (user != null) user.setPassword(null);
        return user;
    }

    private boolean passwordMatches(java.util.Map<String,Object> credentials, String rawPassword) {
        if (credentials == null || rawPassword == null) return false;
        return passwordValueMatches(String.valueOf(credentials.get("password")), rawPassword);
    }

    private boolean passwordValueMatches(String storedPassword, String rawPassword) {
        if (storedPassword == null || rawPassword == null) return false;
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private void migratePlainPasswordIfNeeded(java.util.Map<String,Object> credentials, String rawPassword, Long id, boolean admin) {
        String storedPassword = String.valueOf(credentials.get("password"));
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) return;
        String hashed = passwordEncoder.encode(rawPassword);
        if (admin) authMapper.updateAdminPasswordHash(id, hashed);
        else authMapper.updateUserPasswordHash(id, hashed);
    }
}
