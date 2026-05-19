package com.example.ecommerce.service;

import com.example.ecommerce.mapper.AuthMapper;
import com.example.ecommerce.model.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthMapper authMapper;
    public AuthService(AuthMapper authMapper) { this.authMapper = authMapper; }
    public User loginUser(String username, String password) { return authMapper.findUser(username, password); }
    public User getUser(Long id) { return authMapper.findUserById(id); }
    public User getUserByEmail(String email){return authMapper.findUserByEmail(email);}
    public User register(User user) {
        if (authMapper.findUserByUsername(user.getUsername()) != null) return null;
        authMapper.insertUser(user);
        return user;
    }
    public int updateProfile(User user) { return authMapper.updateProfile(user); }
    public int updatePassword(String email,String password){return authMapper.updatePassword(email,password);}
    public int updatePasswordById(Long id,String oldPassword,String newPassword){return authMapper.updatePasswordById(id,oldPassword,newPassword);}
    public java.util.List<User> findUsers(String keyword, Integer offset, Integer size) { return authMapper.findUsers(keyword, offset, size); }
    public int countUsers(String keyword) { return authMapper.countUsers(keyword); }
    public int updateEnabled(Long id, Boolean enabled) { return authMapper.updateEnabled(id, enabled); }
    public java.util.Map<String,Object> loginAdmin(String username, String password) { return authMapper.findAdminProfileByCredentials(username, password); }
    public int updateAdminPassword(Long id,String oldPassword,String newPassword){return authMapper.updateAdminPassword(id,oldPassword,newPassword);}
    public java.util.Map<String,Object> getAdminProfile(Long id){return authMapper.findAdminProfile(id);}
    public int updateAdminProfile(Long id,String nickname,String email,String phone){return authMapper.updateAdminProfile(id,nickname,email,phone);}
    public AuthMapper getAuthMapper(){return authMapper;}
}
