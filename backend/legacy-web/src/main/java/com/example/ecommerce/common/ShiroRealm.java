package com.example.ecommerce.common;

import com.example.ecommerce.mapper.AuthMapper;
import com.example.ecommerce.service.AuthService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Map;

/**
 * Shiro Realm that handles both:
 * - UsernamePasswordToken (login endpoint: username + password via BCrypt)
 * - SessionToken          (API requests: pre-validated Redis Bearer token)
 */
public class ShiroRealm extends AuthorizingRealm {
    private final AuthService authService;
    private final AuthMapper authMapper;

    public ShiroRealm(AuthService authService, AuthMapper authMapper) {
        this.authService = authService;
        this.authMapper = authMapper;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken || token instanceof SessionToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String principal = (String) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // Principal can be either:
        // - username (from UsernamePasswordToken login)
        // - numeric user/admin ID (from SessionToken / Bearer token)
        Map<String, Object> admin = findAdmin(principal);
        if (admin != null) {
            String role = String.valueOf(admin.get("role"));
            info.addRole(role);
            // Load fine-grained permissions for this role
            java.util.List<String> permissions = authMapper.findPermissionsByRole(role);
            if (permissions != null) {
                permissions.forEach(info::addStringPermission);
            }
            return info;
        }

        // Regular user — only basic USER role
        info.addRole("USER");
        return info;
    }

    /**
     * Look up admin by username (login flow) or ID (Bearer token flow).
     */
    private Map<String, Object> findAdmin(String principal) {
        // Try username lookup first (login flow)
        Map<String, Object> admin = authMapper.findAdminCredentials(principal);
        if (admin != null) return admin;
        // Try ID lookup (Bearer token flow from SessionToken)
        try {
            long id = Long.parseLong(principal);
            return authMapper.findAdminById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // Bearer token flow: AuthInterceptor already validated the Redis session
        if (token instanceof SessionToken st) {
            return new SimpleAuthenticationInfo(
                    st.getPrincipal(), st.getCredentials(), getName());
        }

        // Login flow: username + password verification via BCrypt
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        String password = new String(upToken.getPassword());

        // Try admin login (BCrypt + plaintext migration)
        Map<String, Object> admin = authService.loginAdmin(username, password);
        if (admin != null) {
            // Return token's credentials so Shiro's SimpleCredentialsMatcher passes
            return new SimpleAuthenticationInfo(username, upToken.getCredentials(), getName());
        }

        // Try user login (BCrypt + plaintext migration)
        var user = authService.loginUser(username, password);
        if (user != null) {
            return new SimpleAuthenticationInfo(username, upToken.getCredentials(), getName());
        }

        throw new AuthenticationException("账号或密码错误");
    }
}
