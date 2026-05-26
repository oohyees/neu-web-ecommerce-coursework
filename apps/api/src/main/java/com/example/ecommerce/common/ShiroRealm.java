package com.example.ecommerce.common;

import com.example.ecommerce.mapper.AuthMapper;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Profile("shiro")
public class ShiroRealm extends AuthorizingRealm {
    private final AuthMapper authMapper;

    public ShiroRealm(AuthMapper authMapper) { this.authMapper = authMapper; }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();
        var admin = authMapper.findAdminProfileByCredentials(username, "");
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        if (admin != null) {
            info.addRole(String.valueOf(admin.get("role")));
        }
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        // 尝试管理员登录
        var admin = authMapper.findAdminProfileByCredentials(username, new String(upToken.getPassword()));
        if (admin != null) {
            return new SimpleAuthenticationInfo(username, upToken.getPassword(), getName());
        }
        // 尝试普通用户登录
        var user = authMapper.findUser(username, new String(upToken.getPassword()));
        if (user != null) {
            return new SimpleAuthenticationInfo(username, upToken.getPassword(), getName());
        }
        throw new AuthenticationException("账号或密码错误");
    }
}
