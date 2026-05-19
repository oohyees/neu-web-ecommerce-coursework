package com.example.ecommerce.mapper;

import com.example.ecommerce.model.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface AuthMapper {
    User findUser(@Param("username") String username, @Param("password") String password);
    User findUserById(Long id);
    User findUserByUsername(String username);
    User findUserByEmail(String email);
    int insertUser(User user);
    int updateProfile(User user);
    int updatePassword(@Param("email") String email, @Param("password") String password);
    int updatePasswordById(@Param("id") Long id, @Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword);
    List<User> findUsers(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("size") Integer size);
    int countUsers(@Param("keyword") String keyword);
    int updateEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled);
    Map<String,Object> findAdminProfileByCredentials(@Param("username") String username, @Param("password") String password);
    int updateAdminPassword(@Param("id") Long id, @Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword);
    Map<String,Object> findAdminProfile(Long id);
    int updateAdminProfile(@Param("id") Long id,@Param("nickname") String nickname,@Param("email") String email,@Param("phone") String phone);
    // 管理员账号管理
    List<Map<String,Object>> findAdmins(@Param("keyword") String keyword);
    int insertAdmin(@Param("username") String username, @Param("password") String password, @Param("nickname") String nickname, @Param("email") String email, @Param("phone") String phone, @Param("role") String role);
    int updateAdmin(@Param("id") Long id, @Param("nickname") String nickname, @Param("email") String email, @Param("phone") String phone, @Param("role") String role);
    int deleteAdmin(Long id);
    // 用户管理（admin 侧）
    int deleteUser(Long id);
    int updateUser(@Param("id") Long id, @Param("nickname") String nickname, @Param("email") String email, @Param("phone") String phone);
}
